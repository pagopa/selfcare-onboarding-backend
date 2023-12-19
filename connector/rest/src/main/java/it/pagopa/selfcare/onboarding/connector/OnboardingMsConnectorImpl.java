package it.pagopa.selfcare.onboarding.connector;

import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.onboarding.connector.api.OnboardingMsConnector;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.rest.client.MsOnboardingApiClient;
import it.pagopa.selfcare.onboarding.connector.rest.mapper.OnboardingMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class OnboardingMsConnectorImpl implements OnboardingMsConnector {



    private final MsOnboardingApiClient msOnboardingApiClient;

    private final OnboardingMapper onboardingMapper;

    public OnboardingMsConnectorImpl(MsOnboardingApiClient msOnboardingApiClient, OnboardingMapper onboardingMapper) {
        this.msOnboardingApiClient = msOnboardingApiClient;
        this.onboardingMapper = onboardingMapper;
    }

    @Override
    public void onboarding(OnboardingData onboardingData) {
        if (onboardingData.getInstitutionType() == InstitutionType.PA) {
            msOnboardingApiClient._v1OnboardingPaPost(onboardingMapper.toOnboardingPaRequest(onboardingData));
        } else if (onboardingData.getInstitutionType() == InstitutionType.PSP) {
            msOnboardingApiClient._v1OnboardingPspPost(onboardingMapper.toOnboardingPspRequest(onboardingData));
        } else {
            msOnboardingApiClient._v1OnboardingPost(onboardingMapper.toOnboardingDefaultRequest(onboardingData));
        }
    }

    @Override
    public void onboardingTokenComplete(String onboardingId, MultipartFile contract) {
        msOnboardingApiClient._v1OnboardingOnboardingIdCompletePut(onboardingId, contract);
    }

    @Override
    public void onboardingPending(String onboardingId) {
        msOnboardingApiClient._v1OnboardingOnboardingIdPendingGet(onboardingId);
    }
}
