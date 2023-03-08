package it.pagopa.selfcare.onboarding.connector;

import it.pagopa.selfcare.onboarding.connector.api.MsCoreConnector;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.CreatePnPGInstitutionData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.GeographicTaxonomy;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PnPGOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.onboarding.connector.rest.client.MsCoreRestClient;
import it.pagopa.selfcare.onboarding.connector.rest.model.InstitutionUpdate;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnboardingContract;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnboardingInstitutionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.stream.Collectors;

@Service
@Slf4j
class MsCoreConnectorImpl implements MsCoreConnector {

    protected static final String REQUIRED_INSTITUTION_ID_MESSAGE = "An Institution external id is required";
    protected static final String REQUIRED_DESCRIPTION_MESSAGE = "An Institution decription is required";
    protected static final String REQUIRED_PRODUCT_ID_MESSAGE = "A product Id is required";

    private final MsCoreRestClient restClient;

    @Autowired
    public MsCoreConnectorImpl(MsCoreRestClient restClient) {
        this.restClient = restClient;
    }


    // fixme: PNPGONBOARDINGDATA should be equal to ONBOARDINGDATA (and then the method shoul be renamed) if billingRequest will be refactored to billing
    @Override
    public void onboardingPGOrganization(PnPGOnboardingData onboardingData) {
        Assert.notNull(onboardingData, "Onboarding data is required");
        OnboardingInstitutionRequest onboardingInstitutionRequest = new OnboardingInstitutionRequest();
        onboardingInstitutionRequest.setInstitutionExternalId(onboardingData.getInstitutionExternalId());
        onboardingInstitutionRequest.setBilling(onboardingData.getBillingRequest());
        onboardingInstitutionRequest.setProductId(onboardingData.getProductId());
        onboardingInstitutionRequest.setProductName(onboardingData.getProductName());
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setInstitutionType(onboardingData.getInstitutionType());
        institutionUpdate.setAddress(onboardingData.getInstitutionUpdate().getAddress());
        institutionUpdate.setDescription(onboardingData.getInstitutionUpdate().getDescription());
        institutionUpdate.setDigitalAddress(onboardingData.getInstitutionUpdate().getDigitalAddress());
        institutionUpdate.setTaxCode(onboardingData.getInstitutionUpdate().getTaxCode());
        institutionUpdate.setZipCode(onboardingData.getInstitutionUpdate().getZipCode());
        institutionUpdate.setPaymentServiceProvider(onboardingData.getInstitutionUpdate().getPaymentServiceProvider());
        institutionUpdate.setDataProtectionOfficer(onboardingData.getInstitutionUpdate().getDataProtectionOfficer());
        institutionUpdate.setGeographicTaxonomyCodes(onboardingData.getInstitutionUpdate().getGeographicTaxonomies().stream()
                .map(GeographicTaxonomy::getCode).collect(Collectors.toList()));
        onboardingInstitutionRequest.setInstitutionUpdate(institutionUpdate);

        onboardingInstitutionRequest.setUsers(onboardingData.getUsers().stream()
                .map(userInfo -> {
                    User user = new User();
                    user.setId(userInfo.getId());
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
        onboardingInstitutionRequest.setContract(onboardingContract);

        restClient.onboardingOrganization(onboardingInstitutionRequest);
    }

    @Override
    public Institution getInstitutionByExternalId(String externalInstitutionId) {
        log.trace("getInstitution start");
        log.debug("getInstitution externalInstitutionId = {}", externalInstitutionId);
        Assert.hasText(externalInstitutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        Institution result = restClient.getInstitutionByExternalId(externalInstitutionId);
        log.debug("getInstitution result = {}", result);
        log.trace("getInstitution end");
        return result;
    }

    @Override
    public Institution createPGInstitutionUsingExternalId(CreatePnPGInstitutionData createPnPGData) {
        log.trace("createPGInstitutionUsingExternalId start");
        log.debug("createPGInstitutionUsingExternalId externalId = {}, description = {}", createPnPGData.getTaxId(), createPnPGData.getDescription());
        Assert.hasText(createPnPGData.getTaxId(), REQUIRED_INSTITUTION_ID_MESSAGE);
        Assert.hasText(createPnPGData.getDescription(), REQUIRED_DESCRIPTION_MESSAGE);
        Institution result = restClient.createPGInstitutionUsingExternalId(createPnPGData);
        log.debug("createPGInstitutionUsingExternalId result = {}", result);
        log.trace("createPGInstitutionUsingExternalId end");
        return result;
    }

    @Override
    public void verifyOnboarding(String externalInstitutionId, String productId) {
        log.trace("verifyOnboarding start");
        log.debug("verifyOnboarding externalInstitutionId = {}, productId = {}", externalInstitutionId, productId);
        Assert.hasText(externalInstitutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        Assert.hasText(productId, REQUIRED_PRODUCT_ID_MESSAGE);
        restClient.verifyOnboarding(externalInstitutionId, productId);
        log.trace("verifyOnboarding end");
    }

}
