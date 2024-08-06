package it.pagopa.selfcare.onboarding.connector.api;

import it.pagopa.selfcare.onboarding.connector.model.RecipientCodeStatusResult;
import it.pagopa.selfcare.onboarding.connector.model.institutions.VerifyAggregateResult;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface OnboardingMsConnector {

    void onboarding(OnboardingData onboardingData);

    void onboardingUsers(OnboardingData onboardingData);

    void onboardingCompany(OnboardingData onboardingData);

    void onboardingTokenComplete(String onboardingId, MultipartFile contract);

    void onboardingUsersComplete(String onboardingId, MultipartFile contract);

    void onboardingPending(String onboardingId);

    void approveOnboarding(String onboardingId);

    void rejectOnboarding(String onboardingId, String reason);

    OnboardingData getOnboarding(String onboardingId);

    OnboardingData getOnboardingWithUserInfo(String onboardingId);

    Resource getContract(String onboardingId);

    void onboardingPaAggregation(OnboardingData onboardingData);

    List<OnboardingData> getByFilters(String productId, String taxCode, String origin, String originId, String subunitCode);

    boolean checkManager(OnboardingData onboardingData);

    VerifyAggregateResult verifyAggregatesCsv(MultipartFile file);

    RecipientCodeStatusResult checkRecipientCode(String originId, String recipientCode);

    void verifyOnboarding(String productId, String taxCode, String origin, String originId, String subunitCode);
}
