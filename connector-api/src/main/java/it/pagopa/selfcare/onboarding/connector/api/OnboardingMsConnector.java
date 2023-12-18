package it.pagopa.selfcare.onboarding.connector.api;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import org.springframework.web.multipart.MultipartFile;

public interface OnboardingMsConnector {
    void onboarding(OnboardingData onboardingData);

    void onboardingTokenComplete(String onboardingId, MultipartFile contract);
}