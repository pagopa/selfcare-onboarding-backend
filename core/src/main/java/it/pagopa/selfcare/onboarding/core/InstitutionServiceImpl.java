package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.connector.api.PartyConnector;
import it.pagopa.selfcare.onboarding.connector.api.ProductsConnector;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingResource;
import it.pagopa.selfcare.onboarding.connector.model.product.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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
        Assert.notNull(onboardingData, "Onboarding data is required");

        Product product = productsConnector.getProduct(onboardingData.getProductId());
        Assert.notNull(product, "Product is required");
        onboardingData.setContractPath(product.getContractTemplatePath());
        onboardingData.setContractVersion(product.getContractTemplateVersion());

        Assert.notNull(product.getRoleMappings(), "Role mappings is required");
        onboardingData.getUsers().forEach(userInfo -> {
            Assert.notEmpty(product.getRoleMappings().get(userInfo.getRole()), String.format("At least one Product role related to %s Party role is required", userInfo.getRole()));
            userInfo.setProductRole(product.getRoleMappings().get(userInfo.getRole()).get(0));
        });

        return partyConnector.onboardingOrganization(onboardingData);
    }

}
