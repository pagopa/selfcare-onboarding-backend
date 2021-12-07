package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.connector.api.PartyConnector;
import it.pagopa.selfcare.onboarding.connector.api.ProductsConnector;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingResource;
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
//        Product product = productsConnector.getProduct(productId);
        onboardingData.getUsers().forEach(userInfo -> userInfo.setProductRole(null));//FIXME
        return partyConnector.onboardingOrganization(onboardingData);
    }

}
