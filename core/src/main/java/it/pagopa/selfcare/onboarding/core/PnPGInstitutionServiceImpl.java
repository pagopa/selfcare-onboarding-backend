package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.onboarding.connector.api.MsCoreConnector;
import it.pagopa.selfcare.onboarding.connector.api.PartyRegistryProxyConnector;
import it.pagopa.selfcare.onboarding.connector.api.ProductsConnector;
import it.pagopa.selfcare.onboarding.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.onboarding.connector.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.onboarding.connector.model.PnPGInstitutionLegalAddressData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionPnPGInfo;
import it.pagopa.selfcare.onboarding.connector.model.institutions.PnPGMatchInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.*;
import it.pagopa.selfcare.onboarding.connector.model.product.Product;
import it.pagopa.selfcare.onboarding.connector.model.product.ProductRoleInfo;
import it.pagopa.selfcare.onboarding.connector.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.onboarding.connector.model.user.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;

import static it.pagopa.selfcare.onboarding.connector.model.user.User.Fields.*;
import static it.pagopa.selfcare.onboarding.core.InstitutionServiceImpl.*;

@Slf4j
@Service
class PnPGInstitutionServiceImpl implements PnPGInstitutionService {

    protected static final String REQUIRED_ONBOARDING_DATA_MESSAGE = "Onboarding data is required";

    private static final EnumSet<it.pagopa.selfcare.onboarding.connector.model.user.User.Fields> USER_FIELD_LIST = EnumSet.of(name, familyName, workContacts);

    private final PartyRegistryProxyConnector partyRegistryProxyConnector;
    private final ProductsConnector productsConnector;
    private final MsCoreConnector msCoreConnector;
    private final UserRegistryConnector userConnector;


    @Autowired
    PnPGInstitutionServiceImpl(PartyRegistryProxyConnector partyRegistryProxyConnector,
                               ProductsConnector productsConnector,
                               MsCoreConnector msCoreConnector,
                               UserRegistryConnector userConnector) {
        this.partyRegistryProxyConnector = partyRegistryProxyConnector;
        this.productsConnector = productsConnector;
        this.msCoreConnector = msCoreConnector;
        this.userConnector = userConnector;
    }

    @Override
    public InstitutionPnPGInfo getInstitutionsByUser(User user) {
        log.trace("getInstitutionsByUserId start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionsByUserId user = {}", user);
        InstitutionPnPGInfo result = partyRegistryProxyConnector.getInstitutionsByUserFiscalCode(user.getTaxCode());
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionsByUserId result = {}", result);
        log.trace("getInstitutionsByUserId end");
        return result;
    }

    @Override
    public void onboarding(PnPGOnboardingData onboardingData) {
        log.trace("onboarding PNPG start");
        log.debug("onboarding PNPG onboardingData = {}", onboardingData);
        Assert.notNull(onboardingData, REQUIRED_ONBOARDING_DATA_MESSAGE);

        submitOnboarding(onboardingData);

        log.trace("onboarding PNPG end");
    }

    private void submitOnboarding(PnPGOnboardingData onboardingData) {
        log.trace("submitOnboarding PNPG start");

        Product product = productsConnector.getProduct(onboardingData.getProductId(), InstitutionType.PG);
        Assert.notNull(product, "Product is required");

        final EnumMap<PartyRole, ProductRoleInfo> roleMappings = product.getRoleMappings();
        Assert.notNull(roleMappings, "Role mappings is required");
        onboardingData.getUsers().forEach(userInfo -> {
            Assert.notNull(roleMappings.get(userInfo.getRole()),
                    String.format(ATLEAST_ONE_PRODUCT_ROLE_REQUIRED, userInfo.getRole()));
            Assert.notEmpty(roleMappings.get(userInfo.getRole()).getRoles(),
                    String.format(ATLEAST_ONE_PRODUCT_ROLE_REQUIRED, userInfo.getRole()));
            Assert.state(roleMappings.get(userInfo.getRole()).getRoles().size() == 1,
                    String.format(MORE_THAN_ONE_PRODUCT_ROLE_AVAILABLE, userInfo.getRole()));
            userInfo.setProductRole(roleMappings.get(userInfo.getRole()).getRoles().get(0).getCode());
        });

        CreatePnPGInstitutionData createPGData = mapCreatePnPGInstitutionData(onboardingData);
        Institution institution;
        try {
            institution = msCoreConnector.getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        } catch (ResourceNotFoundException e) {
            institution = msCoreConnector.createPGInstitutionUsingExternalId(createPGData);
        }

        onboardingData.setInstitutionUpdate(mapInstitutionToInstitutionUpdate(institution));

        String finalInstitutionInternalId = institution.getId();
        onboardingData.getUsers().forEach(user -> {
            final Optional<it.pagopa.selfcare.onboarding.connector.model.user.User> searchResult =
                    userConnector.search(user.getTaxCode(), USER_FIELD_LIST);
            searchResult.ifPresentOrElse(foundUser -> {
                Optional<MutableUserFieldsDto> updateRequest = createUpdateRequest(user, foundUser, finalInstitutionInternalId);
                updateRequest.ifPresent(mutableUserFieldsDto ->
                        userConnector.updateUser(UUID.fromString(foundUser.getId()), mutableUserFieldsDto));
                user.setId(foundUser.getId());
            }, () -> user.setId(userConnector.saveUser(UserMapper.toSaveUserDto(user, finalInstitutionInternalId))
                    .getId().toString()));
        });

        msCoreConnector.onboardingPGOrganization(onboardingData);
        log.trace("submitOnboarding PNPG start");
    }

    private CreatePnPGInstitutionData mapCreatePnPGInstitutionData(PnPGOnboardingData onboardingData) {
        CreatePnPGInstitutionData createPGData = new CreatePnPGInstitutionData();
        createPGData.setDescription(onboardingData.getBusinessName());
        createPGData.setTaxId(onboardingData.getInstitutionExternalId());
        createPGData.setCertified(onboardingData.isCertified());
        return createPGData;
    }

    private InstitutionUpdate mapInstitutionToInstitutionUpdate(Institution institution) {
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setTaxCode(institution.getTaxCode());
        institutionUpdate.setInstitutionType(InstitutionType.PG);
        institutionUpdate.setGeographicTaxonomies(new ArrayList<>());
        return institutionUpdate;
    }

    @Override
    public PnPGMatchInfo matchInstitutionAndUser(String externalInstitutionId, User user) {
        log.trace("matchInstitutionAndUser start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "matchInstitutionAndUser user = {}", user);
        PnPGMatchInfo result = partyRegistryProxyConnector.matchInstitutionAndUser(externalInstitutionId, user.getTaxCode());
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "matchInstitutionAndUser result = {}", result);
        log.trace("matchInstitutionAndUser end");
        return result;
    }

    @Override
    public PnPGInstitutionLegalAddressData getInstitutionLegalAddress(String externalInstitutionId) {
        log.trace("getInstitutionLegalAddress start");
        log.debug("getInstitutionLegalAddress externalInstitutionId = {}", externalInstitutionId);
        PnPGInstitutionLegalAddressData result = partyRegistryProxyConnector.getInstitutionLegalAddress(externalInstitutionId);
        log.debug("getInstitutionLegalAddress result = {}", result);
        log.trace("getInstitutionLegalAddress end");
        return result;
    }

}
