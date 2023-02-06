package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.onboarding.connector.api.MsCoreConnector;
import it.pagopa.selfcare.onboarding.connector.api.PartyRegistryProxyConnector;
import it.pagopa.selfcare.onboarding.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.onboarding.connector.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionPnPGInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionType;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionUpdate;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PnPGOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.onboarding.connector.model.product.ProductRoleInfo;
import it.pagopa.selfcare.onboarding.connector.model.user.Certification;
import it.pagopa.selfcare.onboarding.connector.model.user.CertifiedField;
import it.pagopa.selfcare.onboarding.connector.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.onboarding.connector.model.user.WorkContact;
import it.pagopa.selfcare.onboarding.connector.model.user.mapper.CertifiedFieldMapper;
import it.pagopa.selfcare.onboarding.connector.model.user.mapper.UserMapper;
import it.pagopa.selfcare.onboarding.core.exception.UpdateNotAllowedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.validation.ValidationException;
import java.util.*;

import static it.pagopa.selfcare.onboarding.connector.model.user.User.Fields.*;
import static it.pagopa.selfcare.onboarding.core.InstitutionServiceImpl.ATLEAST_ONE_PRODUCT_ROLE_REQUIRED;
import static it.pagopa.selfcare.onboarding.core.InstitutionServiceImpl.MORE_THAN_ONE_PRODUCT_ROLE_AVAILABLE;

@Slf4j
@Service
class PnPGInstitutionServiceImpl implements PnPGInstitutionService {

    private static final EnumSet<it.pagopa.selfcare.onboarding.connector.model.user.User.Fields> USER_FIELD_LIST = EnumSet.of(name, familyName, workContacts);
    private static final EnumSet<it.pagopa.selfcare.onboarding.connector.model.user.User.Fields> USER_FIELD_ONLY_FISCAL_CODE = EnumSet.of(fiscalCode);

    private final PartyRegistryProxyConnector partyRegistryProxyConnector;
    private final MsCoreConnector msCoreConnector;
    private final UserRegistryConnector userConnector;


    @Autowired
    PnPGInstitutionServiceImpl(PartyRegistryProxyConnector partyRegistryProxyConnector,
                               MsCoreConnector msCoreConnector,
                               UserRegistryConnector userConnector) {
        this.partyRegistryProxyConnector = partyRegistryProxyConnector;
        this.msCoreConnector = msCoreConnector;
        this.userConnector = userConnector;
    }

    @Override
    public InstitutionPnPGInfo getInstitutionsByUser(User user) {
        log.trace("getInstitutionsByUserId start");
        log.debug("getInstitutionsByUserId user = {}", user);
        user.setId(userConnector.saveUser(UserMapper.toSaveUserDto(user, "")).getId().toString());
        InstitutionPnPGInfo result = partyRegistryProxyConnector.getInstitutionsByUserFiscalCode(user.getTaxCode());
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionsByUserId result = {}", result);
        log.trace("getInstitutionsByUserId end");
        return result;
    }

    @Override
    public void onboarding(PnPGOnboardingData onboardingData) {
        log.trace("onboarding PNPG start");
        log.debug("onboarding PNPG onboardingData = {}", onboardingData);
//        Assert.notNull(onboardingData, REQUIRED_ONBOARDING_DATA_MESSAGE);
//        Assert.notNull(onboardingData.getBilling(), REQUIRED_INSTITUTION_BILLING_DATA_MESSAGE);
//        Assert.notNull(onboardingData.getInstitutionType(), REQUIRED_INSTITUTION_TYPE_MESSAGE);

        onboardingData.setProductName("prod-pn-pg"); // fixme: retrieve from db?
        onboardingData.setContractPath("mock"); // fixme: retrieve from db?
        onboardingData.setContractVersion("mock");  // fixme: retrieve from db?

        try {
            submitOnboarding(onboardingData);
        } catch (RuntimeException e) {
            throw new ValidationException(String.format("Unable to onboard a PG Institution (external id: '%s') already onboarded",
                    onboardingData.getInstitutionExternalId()));
        }

        log.trace("onboarding PNPG end");
    }

    private void submitOnboarding(PnPGOnboardingData onboardingData) {
        log.trace("submitOnboarding PNPG start");
        final EnumMap<PartyRole, ProductRoleInfo> roleMappings = mockRoleMapPnPGProduct();
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

        Institution institution;
        try {
            institution = msCoreConnector.getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        } catch (ResourceNotFoundException e) {
            institution = msCoreConnector.createPGInstitutionUsingExternalId(onboardingData.getInstitutionExternalId());
        }

        onboardingData.setInstitutionUpdate(mockMapInstitutionToInstitutionUpdate(institution));

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

    private InstitutionUpdate mapInstitutionToInstitutionUpdate(Institution institution) {
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setAddress(institution.getAddress());
        institutionUpdate.setDescription(institution.getDescription());
        institutionUpdate.setDigitalAddress(institution.getDigitalAddress());
        institutionUpdate.setTaxCode(institution.getTaxCode());
        institutionUpdate.setZipCode(institution.getZipCode());
        institutionUpdate.setPaymentServiceProvider(institution.getPaymentServiceProvider());
        institutionUpdate.setDataProtectionOfficer(institution.getDataProtectionOfficer());
        institutionUpdate.setGeographicTaxonomies(institution.getGeographicTaxonomies());
        return institutionUpdate;
    }

    private InstitutionUpdate mockMapInstitutionToInstitutionUpdate(Institution institution) {
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setTaxCode(institution.getTaxCode());
        institutionUpdate.setInstitutionType(InstitutionType.PG);
        institutionUpdate.setGeographicTaxonomies(new ArrayList<>());
        return institutionUpdate;
    }

    private EnumMap<PartyRole, ProductRoleInfo> mockRoleMapPnPGProduct() {
        ProductRoleInfo.ProductRole role = new ProductRoleInfo.ProductRole();
        role.setCode("referente amministrativo");
        role.setLabel("Amministratore");
        role.setDescription("Amministratore");
        List<ProductRoleInfo.ProductRole> roleList = new ArrayList<>();
        roleList.add(role);
        ProductRoleInfo productRoleInfo = new ProductRoleInfo();
        productRoleInfo.setRoles(roleList);
        final EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class);
        roleMappings.put(PartyRole.MANAGER, productRoleInfo);

        return roleMappings;
    }

    private Optional<MutableUserFieldsDto> createUpdateRequest(User user, it.pagopa.selfcare.onboarding.connector.model.user.User foundUser, String institutionInternalId) {
        Optional<MutableUserFieldsDto> mutableUserFieldsDto = Optional.empty();
        if (isFieldToUpdate(foundUser.getName(), user.getName())) {
            MutableUserFieldsDto dto = new MutableUserFieldsDto();
            dto.setName(CertifiedFieldMapper.map(user.getName()));
            mutableUserFieldsDto = Optional.of(dto);
        }
        if (isFieldToUpdate(foundUser.getFamilyName(), user.getSurname())) {
            MutableUserFieldsDto dto = mutableUserFieldsDto.orElseGet(MutableUserFieldsDto::new);
            dto.setFamilyName(CertifiedFieldMapper.map(user.getSurname()));
            mutableUserFieldsDto = Optional.of(dto);
        }
        if (foundUser.getWorkContacts() == null
                || !foundUser.getWorkContacts().containsKey(institutionInternalId)
                || isFieldToUpdate(foundUser.getWorkContacts().get(institutionInternalId).getEmail(), user.getEmail())) {
            MutableUserFieldsDto dto = mutableUserFieldsDto.orElseGet(MutableUserFieldsDto::new);
            final WorkContact workContact = new WorkContact();
            workContact.setEmail(CertifiedFieldMapper.map(user.getEmail()));
            dto.setWorkContacts(Map.of(institutionInternalId, workContact));
            mutableUserFieldsDto = Optional.of(dto);
        }
        return mutableUserFieldsDto;
    }

    private boolean isFieldToUpdate(CertifiedField<String> certifiedField, String value) {
        boolean isToUpdate = true;
        if (certifiedField != null) {
            if (Certification.NONE.equals(certifiedField.getCertification())) {
                if (certifiedField.getValue().equals(value)) {
                    isToUpdate = false;
                }
            } else {
                if (certifiedField.getValue().equalsIgnoreCase(value)) {
                    isToUpdate = false;
                } else {
                    throw new UpdateNotAllowedException(String.format("Update user request not allowed because of value %s", value));
                }
            }
        }
        return isToUpdate;
    }


}
