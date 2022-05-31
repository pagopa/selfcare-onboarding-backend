package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
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
import it.pagopa.selfcare.onboarding.connector.model.user.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

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

    private static final EnumSet<it.pagopa.selfcare.onboarding.connector.model.user.User.Fields> USER_FIELD_LIST = EnumSet.of(fiscalCode, name, familyName, workContacts);
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
        onboardingData.getUsers().forEach(user ->
                user.setId(userConnector.saveUser(UserMapper.toSaveUserDto(user, finalInstitutionInternalId))
                        .getId().toString()));
        partyConnector.onboardingOrganization(onboardingData);
        log.trace("onboarding end");
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
                    userConnector.getUserByInternalId(response.get(0).getFrom(), USER_FIELD_LIST);
            User manager = new User();
            manager.setId(baseProductManager.getId());
            manager.setName(baseProductManager.getName().getValue());
            manager.setSurname(baseProductManager.getFamilyName().getValue());
            manager.setTaxCode(baseProductManager.getFiscalCode());
            manager.setProduct(onboardingData.getProductId());
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
        log.trace("getManager start");
        log.debug("getManager externalInstitutionId = {}, productId = {}", externalInstitutionId, productId);
        Assert.hasText(externalInstitutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        InstitutionOnboardingData result = new InstitutionOnboardingData();

        EnumSet<PartyRole> roles = Arrays.stream(PartyRole.values())
                .filter(partyRole -> SelfCareAuthority.ADMIN.equals(partyRole.getSelfCareAuthority()))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(PartyRole.class)));
        if (checkAuthority(externalInstitutionId, productId, roles)) {
            UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
            userInfoFilter.setProductId(Optional.of(productId));
            userInfoFilter.setRole(MANAGER_ROLE_FILTER);
            userInfoFilter.setAllowedStates(ACTIVE_ALLOWED_STATES_FILTER);
            Collection<UserInfo> userInfos = partyConnector.getUsers(externalInstitutionId, userInfoFilter).stream()
                    .peek(userInfo -> userInfo.setUser(userConnector.getUserByInternalId(userInfo.getId(), USER_FIELD_LIST)))
                    .collect(Collectors.toList());
            if (!userInfos.iterator().hasNext()) {
                throw new ResourceNotFoundException(String.format("No Manager found for given institution: %s", externalInstitutionId));
            }
            UserInfo manager = userInfos.iterator().next();
            result.setManager(manager);
        }
        InstitutionInfo institution = partyConnector.getOnboardedInstitution(externalInstitutionId);
        if (institution == null) {
            throw new ResourceNotFoundException(String.format("Institution %s not found", externalInstitutionId));
        }
        result.setInstitution(institution);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getManager result = {}", result);
        log.trace("getManager end");
        return result;
    }


    private Boolean checkAuthority(String externalInstitutionId, String productId, EnumSet<PartyRole> roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assert.state(authentication != null, "Authentication is required");
        Assert.state(authentication.getPrincipal() instanceof SelfCareUser, "Not SelfCareUser principal");
        SelfCareUser principal = ((SelfCareUser) authentication.getPrincipal());
        UserInfo.UserInfoFilter filter = new UserInfo.UserInfoFilter();
        filter.setUserId(Optional.of(principal.getId()));
        filter.setProductId(Optional.of(productId));
        filter.setAllowedStates(ACTIVE_ALLOWED_STATES_FILTER);
        filter.setRole(Optional.of(roles));
        Collection<UserInfo> userInfos = partyConnector.getUsers(externalInstitutionId, filter);
        return userInfos.iterator().hasNext();
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
