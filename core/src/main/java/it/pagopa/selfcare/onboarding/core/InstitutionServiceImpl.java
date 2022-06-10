package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.onboarding.connector.api.PartyConnector;
import it.pagopa.selfcare.onboarding.connector.api.ProductsConnector;
import it.pagopa.selfcare.onboarding.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.onboarding.connector.exceptions.ManagerNotFoundException;
import it.pagopa.selfcare.onboarding.connector.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipState;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipsResponse;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PartyRole;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
import it.pagopa.selfcare.onboarding.connector.model.product.Product;
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

import java.util.*;

import static it.pagopa.selfcare.onboarding.connector.model.user.User.Fields.*;

@Slf4j
@Service
class InstitutionServiceImpl implements InstitutionService {

    protected static final String REQUIRED_INSTITUTION_ID_MESSAGE = "An Institution id is required";
    protected static final String REQUIRED_INSTITUTION_BILLING_DATA_MESSAGE = "Institution's billing data are required";
    protected static final String REQUIRED_INSTITUTION_TYPE_MESSAGE = "An institution type is required";
    protected static final String REQUIRED_ONBOARDING_DATA_MESSAGE = "Onboarding data is required";
    protected static final String ATLEAST_ONE_PRODUCT_ROLE_REQUIRED = "At least one Product role related to %s Party role is required";
    protected static final String MORE_THAN_ONE_PRODUCT_ROLE_AVAILABLE = "More than one Product role related to %s Party role is available. Cannot automatically set the Product role";

    private static final EnumSet<it.pagopa.selfcare.onboarding.connector.model.user.User.Fields> USER_FIELD_LIST_ENHANCED = EnumSet.of(fiscalCode, name, familyName, workContacts);
    private static final EnumSet<it.pagopa.selfcare.onboarding.connector.model.user.User.Fields> USER_FIELD_LIST = EnumSet.of(name, familyName, workContacts);
    private static final Optional<EnumSet<PartyRole>> MANAGER_ROLE_FILTER = Optional.of(EnumSet.of(PartyRole.MANAGER));
    private static final Optional<EnumSet<RelationshipState>> ACTIVE_ALLOWED_STATES_FILTER = Optional.of(EnumSet.of(RelationshipState.ACTIVE));

    private final PartyConnector partyConnector;
    private final ProductsConnector productsConnector;
    private final UserRegistryConnector userConnector;


    @Autowired
    InstitutionServiceImpl(PartyConnector partyConnector, ProductsConnector productsConnector, UserRegistryConnector userConnector) {
        this.partyConnector = partyConnector;
        this.productsConnector = productsConnector;
        this.userConnector = userConnector;
    }


    @Override
    public void onboarding(OnboardingData onboardingData) {
        log.trace("onboarding start");
        log.debug("onboarding onboardingData = {}", onboardingData);
        Assert.notNull(onboardingData, REQUIRED_ONBOARDING_DATA_MESSAGE);
        Assert.notNull(onboardingData.getBilling(), REQUIRED_INSTITUTION_BILLING_DATA_MESSAGE);
        Assert.notNull(onboardingData.getInstitutionType(), REQUIRED_INSTITUTION_TYPE_MESSAGE);
        Product product = productsConnector.getProduct(onboardingData.getProductId());
        Assert.notNull(product, "Product is required");
        onboardingData.setContractPath(product.getContractTemplatePath());
        onboardingData.setContractVersion(product.getContractTemplateVersion());

        final EnumMap<PartyRole, ProductRoleInfo> roleMappings;
        if (product.getParentId() != null) {
            final Product baseProduct = productsConnector.getProduct(product.getParentId());
            final Optional<User> manager = retrieveManager(onboardingData, baseProduct);
            onboardingData.setUsers(List.of(manager.orElseThrow(() ->
                    new ManagerNotFoundException(String.format("Unable to retrieve the manager related to institution external id = %s and base product %s",
                            onboardingData.getInstitutionExternalId(),
                            baseProduct.getId())))));
            roleMappings = baseProduct.getRoleMappings();
        } else {
            roleMappings = product.getRoleMappings();
        }
        onboardingData.setProductName(product.getTitle());
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
            institution = partyConnector.getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        } catch (ResourceNotFoundException e) {
            institution = partyConnector.createInstitutionUsingExternalId(onboardingData.getInstitutionExternalId());
        }
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
        partyConnector.onboardingOrganization(onboardingData);
        log.trace("onboarding end");
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
                if (certifiedField.getValue().equals(value)) {
                    isToUpdate = false;
                } else {
                    throw new UpdateNotAllowedException(String.format("Update user request not allowed because of value %s", value));
                }
            }
        }
        return isToUpdate;
    }


    private Optional<User> retrieveManager(OnboardingData onboardingData, Product product) {
        Optional<User> managerOpt = Optional.empty();
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setRole(MANAGER_ROLE_FILTER);
        userInfoFilter.setAllowedStates(ACTIVE_ALLOWED_STATES_FILTER);
        userInfoFilter.setProductId(Optional.of(product.getId()));
        RelationshipsResponse response = partyConnector.getUserInstitutionRelationships(onboardingData.getInstitutionExternalId(), userInfoFilter);

        if (response != null && response.size() == 1) {
            final it.pagopa.selfcare.onboarding.connector.model.user.User baseProductManager =
                    userConnector.getUserByInternalId(response.get(0).getFrom(), USER_FIELD_LIST_ENHANCED);
            User manager = new User();
            manager.setId(baseProductManager.getId());
            manager.setName(baseProductManager.getName().getValue());
            manager.setSurname(baseProductManager.getFamilyName().getValue());
            manager.setTaxCode(baseProductManager.getFiscalCode());
            manager.setRole(PartyRole.MANAGER);
            manager.setEmail(baseProductManager.getWorkContacts().get(response.get(0).getTo()).getEmail().getValue());
            String productRole = product.getRoleMappings().get(PartyRole.MANAGER).getRoles().get(0).getCode();
            manager.setProductRole(productRole);

            managerOpt = Optional.of(manager);
        }
        return managerOpt;
    }


    @Override
    public Collection<InstitutionInfo> getInstitutions() {
        log.trace("getInstitutions start");
        Collection<InstitutionInfo> result = partyConnector.getOnBoardedInstitutions();
        log.debug("getInstitutions result = {}", result);
        log.trace("getInstitutions end");
        return result;
    }


    @Override
    public InstitutionOnboardingData getInstitutionOnboardingData(String externalInstitutionId, String productId) {
        log.trace("getInstitutionOnboardingData start");
        log.debug("getInstitutionOnboardingData externalInstitutionId = {}, productId = {}", externalInstitutionId, productId);
        Assert.hasText(externalInstitutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        InstitutionOnboardingData result = new InstitutionOnboardingData();

        final EnumSet<it.pagopa.selfcare.onboarding.connector.model.user.User.Fields> fieldList = EnumSet.of(name, familyName, workContacts, fiscalCode);
        UserInfo manager = partyConnector.getInstitutionManager(externalInstitutionId, productId);
        if (manager == null) {
            throw new ResourceNotFoundException(String.format("No Manager found for given institution: %s", externalInstitutionId));
        }
        manager.setUser(userConnector.getUserByInternalId(manager.getId(), fieldList));
        result.setManager(manager);

        InstitutionInfo institution = partyConnector.getOnboardedInstitution(externalInstitutionId);
        if (institution == null) {
            throw new ResourceNotFoundException(String.format("Institution %s not found", externalInstitutionId));
        }
        result.setInstitution(institution);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionOnboardingData result = {}", result);
        log.trace("getInstitutionOnboardingData end");
        return result;
    }


    @Override
    public Institution getInstitutionByExternalId(String externalInstitutionId) {
        log.trace("getInstitutionData start");
        log.debug("getInstitutionData externalInstitutionId = {}", externalInstitutionId);
        Assert.hasText(externalInstitutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        Institution institution = partyConnector.getInstitutionByExternalId(externalInstitutionId);
        log.debug("getInstitutionData result = {}", institution);
        log.trace("getInstitutionData end");
        return institution;
    }

}
