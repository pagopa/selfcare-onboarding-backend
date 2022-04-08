package it.pagopa.selfcare.onboarding.connector;

import it.pagopa.selfcare.onboarding.connector.api.PartyConnector;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnboardingContract;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnboardingRequest;
import it.pagopa.selfcare.onboarding.connector.rest.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.stream.Collectors;

@Service
class PartyConnectorImpl implements PartyConnector {

    private final PartyProcessRestClient restClient;


    @Autowired
    public PartyConnectorImpl(PartyProcessRestClient restClient) {
        this.restClient = restClient;
    }


    @Override
    public void onboardingOrganization(OnboardingData onboardingData) {
        Assert.notNull(onboardingData, "Onboarding data is required");
        OnboardingRequest onboardingRequest = new OnboardingRequest();
        onboardingRequest.setInstitutionId(onboardingData.getInstitutionId());
        onboardingRequest.setUsers(onboardingData.getUsers().stream()
                .map(userInfo -> {
                    User user = new User();
                    user.setProduct(onboardingData.getProductId());
                    user.setName(userInfo.getName());
                    user.setSurname(userInfo.getSurname());
                    user.setTaxCode(userInfo.getTaxCode());
                    user.setEmail(userInfo.getEmail());
                    user.setRole(userInfo.getRole());
                    user.setProductRole(userInfo.getProductRole());
                    return user;
                }).collect(Collectors.toList()));
        OnboardingContract onboardingContract = new OnboardingContract();
        onboardingContract.setPath(onboardingData.getContractPath());
        onboardingContract.setVersion(onboardingData.getContractVersion());
        onboardingRequest.setContract(onboardingContract);

        restClient.onboardingOrganization(onboardingRequest);
    }

}
