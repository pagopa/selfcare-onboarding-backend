package it.pagopa.selfcare.onboarding.connector.api;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface OnboardingMsConnector {
    void onboarding(OnboardingData onboardingData);

    void onboardingCompany(OnboardingData onboardingData);

    void onboardingTokenComplete(String onboardingId, MultipartFile contract);

    void onboardingPending(String onboardingId);

    void approveOnboarding(String onboardingId);

    void rejectOnboarding(String onboardingId, String reason);

    OnboardingData getOnboarding(String onboardingId);

    OnboardingData getOnboardingWithUserInfo(String onboardingId);

    Resource getContract(String onboardingId);
}
