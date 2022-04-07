package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.onboarding.connector.api.PartyConnector;
import it.pagopa.selfcare.onboarding.connector.api.ProductsConnector;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipState;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipsResponse;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingResource;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PartyRole;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
import it.pagopa.selfcare.onboarding.connector.model.product.Product;
import it.pagopa.selfcare.onboarding.core.exceptions.ProductHasNoRelationshipException;
import it.pagopa.selfcare.onboarding.core.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.validation.ValidationException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;

@Slf4j
@Service
class InstitutionServiceImpl implements InstitutionService {

    protected static final String REQUIRED_INSTITUTION_ID_MESSAGE = "An Institution id is required";

    private final PartyConnector partyConnector;
    private final ProductsConnector productsConnector;


    @Autowired
    InstitutionServiceImpl(PartyConnector partyConnector, ProductsConnector productsConnector) {
        this.partyConnector = partyConnector;
        this.productsConnector = productsConnector;
    }


    @Override
    public OnboardingResource onboarding(OnboardingData onboardingData) {
        log.trace("onboarding start");
        log.debug("onboarding onboardingData = {}", onboardingData);
        Assert.notNull(onboardingData, "Onboarding data is required");
        Product product = productsConnector.getProduct(onboardingData.getProductId());
        Assert.notNull(product, "Product is required");
        onboardingData.setContractPath(product.getContractTemplatePath());
        onboardingData.setContractVersion(product.getContractTemplateVersion());

        if (product.getParent() != null) {
            //TODO organizationType has to be retrieved from the institution, by calling the getInstitution API in party-process
            RelationshipsResponse response = partyConnector.getUserInstitutionRelationships(onboardingData.getInstitutionId()
                    , product.getParent());
            if (response == null) {
                throw new ProductHasNoRelationshipException(
                        String.format("No relationship for %s and %s", product.getParent(), onboardingData.getInstitutionId())
                );
            } else {
                Assert.isTrue(onboardingData.getUsers().stream()
                                .filter(user -> PartyRole.MANAGER.equals(user.getRole()))
                                .findAny().orElseThrow(() -> new ValidationException("")).getTaxCode().equals(response.stream()//TODO exception messagre
                                        .filter(relationshipInfo -> PartyRole.MANAGER.equals(relationshipInfo.getRole()))
                                        .findAny().orElseThrow(() -> new ValidationException("")).getTaxCode())//TODO exception messagre
                        , "");//TODO message

            }
            // else response.get(0).getTaxCode() == onboardingData.getUsers(.get(0)).getTaxCode()
            product = productsConnector.getProduct(product.getParent());
        }
        //TODO in else check onboardingData.getBillingData assertNotNull OnboardingData.getBillingData
        //TODO in else check onboardingData.organizationType(mandatory for base contract)
        Assert.notNull(product.getRoleMappings(), "Role mappings is required");
        Product finalProduct = product;
        onboardingData.getUsers().forEach(userInfo -> {
            Assert.notNull(finalProduct.getRoleMappings().get(userInfo.getRole()),
                    String.format("At least one Product role related to %s Party role is required", userInfo.getRole()));
            Assert.notEmpty(finalProduct.getRoleMappings().get(userInfo.getRole()).getRoles(),
                    String.format("At least one Product role related to %s Party role is required", userInfo.getRole()));
            Assert.state(finalProduct.getRoleMappings().get(userInfo.getRole()).getRoles().size() == 1,
                    String.format("More than one Product role related to %s Party role is available. Cannot automatically set the Product role", userInfo.getRole()));
            userInfo.setProductRole(finalProduct.getRoleMappings().get(userInfo.getRole()).getRoles().get(0).getCode());
        });

        OnboardingResource result = partyConnector.onboardingOrganization(onboardingData);
        log.debug("onboarding result = {}", result);
        log.trace("onboarding end");
        return result;
    }

    @Override
    public Collection<InstitutionInfo> getInstitutions() {
        log.trace("getInstitutions start");
        Collection<InstitutionInfo> result = partyConnector.getOnBoardedInstitutions();
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutions result = {}", result);
        log.trace("getInstitutions end");
        return result;
    }

    @Override
    public UserInfo getManager(String institutionId, String productId) {
        log.trace("getManager start");
        log.debug("getManager institutionId = {}, productId = {}", institutionId, productId);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(Optional.of(productId));
        userInfoFilter.setRole(Optional.of(PartyRole.MANAGER));
        userInfoFilter.setAllowedState(Optional.of(EnumSet.of(RelationshipState.ACTIVE)));
        Collection<UserInfo> userInfos = getUsers(institutionId, userInfoFilter);
        if (!userInfos.iterator().hasNext()) {
            throw new ResourceNotFoundException("No Manager found for given institution");
        }
        UserInfo result = userInfos.iterator().next();
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getManager result = {}", result);
        log.trace("getManager end");
        return result;
    }

    private Collection<UserInfo> getUsers(String institutionId, UserInfo.UserInfoFilter userInfoFilter) {
        log.trace("getUsers start");
        log.debug("getUsers institutionId = {}, productId = {}, role = {}, productRoles = {}",
                institutionId, userInfoFilter.getProductId(), userInfoFilter.getRole(), userInfoFilter.getProductRoles());
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUsers result = {}", userInfos);
        log.trace("getUsers end");
        return userInfos;
    }

}
