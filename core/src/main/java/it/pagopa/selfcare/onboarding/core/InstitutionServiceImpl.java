package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.onboarding.connector.api.PartyConnector;
import it.pagopa.selfcare.onboarding.connector.api.ProductsConnector;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipsResponse;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingResource;
import it.pagopa.selfcare.onboarding.connector.model.product.Product;
import it.pagopa.selfcare.onboarding.core.exceptions.ProductHasNoRelationshipException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collection;

@Slf4j
@Service
class InstitutionServiceImpl implements InstitutionService {

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
            RelationshipsResponse response = partyConnector.getUserInstitutionRelationships(onboardingData.getInstitutionId()
                    , product.getParent());
            if (response == null) {
                throw new ProductHasNoRelationshipException("No relationship for "
                        + product.getParent()
                        + " and "
                        + onboardingData.getInstitutionId()
                );
            }
            product = productsConnector.getProduct(product.getParent());
        }
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

}
