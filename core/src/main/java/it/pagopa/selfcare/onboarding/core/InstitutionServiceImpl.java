package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.onboarding.connector.api.PartyConnector;
import it.pagopa.selfcare.onboarding.connector.api.ProductsConnector;
import it.pagopa.selfcare.onboarding.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipState;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipsResponse;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PartyRole;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
import it.pagopa.selfcare.onboarding.connector.model.product.Product;
import it.pagopa.selfcare.onboarding.connector.model.user.User;
import it.pagopa.selfcare.onboarding.core.exceptions.InternalServerException;
import it.pagopa.selfcare.onboarding.core.exceptions.ProductHasNoRelationshipException;
import it.pagopa.selfcare.onboarding.core.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.validation.ValidationException;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;
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
    protected static final String ILLEGAL_LIST_OF_USERS = "Illegal list of users, provide a Manager in the list";
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

        if (product.getParentId() != null) {
            RelationshipsResponse response = partyConnector.getUserInstitutionRelationships(onboardingData.getInstitutionId(), product.getParentId());
            if (response == null) {
                throw new ProductHasNoRelationshipException(
                        String.format("No relationship for %s and %s", product.getParentId(), onboardingData.getInstitutionId())
                );
            } else {
                if (!onboardingData.getUsers().stream()
                        .filter(user -> PartyRole.MANAGER.equals(user.getRole()))
                        .findAny()
                        .orElseThrow(() -> new ValidationException(ILLEGAL_LIST_OF_USERS)).getTaxCode().equals(response.stream()
                                .filter(relationshipInfo -> PartyRole.MANAGER.equals(relationshipInfo.getRole()))
                                .findAny()//TODO: correct if can exists only one manager for each institution-product pair
                                .map(relationshipInfo -> userConnector.getUserByInternalId(relationshipInfo.getTo(), EnumSet.of(fiscalCode)))
                                .orElseThrow(() -> new InternalServerException("Internal Error: Legal referent not Manager")).getFiscalCode())
                ) throw new ValidationException("The provided Manager is not valid for this product");

            }
            product = productsConnector.getProduct(product.getParentId());
        }
        Assert.notNull(product.getRoleMappings(), "Role mappings is required");
        Product finalProduct = product;
        onboardingData.getUsers().forEach(userInfo -> {
            Assert.notNull(finalProduct.getRoleMappings().get(userInfo.getRole()),
                    String.format(ATLEAST_ONE_PRODUCT_ROLE_REQUIRED, userInfo.getRole()));
            Assert.notEmpty(finalProduct.getRoleMappings().get(userInfo.getRole()).getRoles(),
                    String.format(ATLEAST_ONE_PRODUCT_ROLE_REQUIRED, userInfo.getRole()));
            Assert.state(finalProduct.getRoleMappings().get(userInfo.getRole()).getRoles().size() == 1,
                    String.format(MORE_THAN_ONE_PRODUCT_ROLE_AVAILABLE, userInfo.getRole()));
            userInfo.setProductRole(finalProduct.getRoleMappings().get(userInfo.getRole()).getRoles().get(0).getCode());
        });

        partyConnector.onboardingOrganization(onboardingData);
        log.trace("onboarding end");
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
            userInfoFilter.setRole(Optional.of(EnumSet.of(PartyRole.MANAGER)));
            userInfoFilter.setAllowedState(Optional.of(EnumSet.of(RelationshipState.ACTIVE)));
            final EnumSet<User.Fields> fieldList = EnumSet.of(name, familyName, workContacts, fiscalCode);
            Collection<UserInfo> userInfos = partyConnector.getUsers(externalInstitutionId, userInfoFilter).stream()
                    .peek(userInfo -> userInfo.setUser(userConnector.getUserByInternalId(userInfo.getId(), fieldList)))
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
        filter.setAllowedState(Optional.of(EnumSet.of(RelationshipState.ACTIVE)));
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
