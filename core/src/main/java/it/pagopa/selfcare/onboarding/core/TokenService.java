package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface TokenService {
  OnboardingData verifyOnboarding(String onboardingId);

  void approveOnboarding(String onboardingId);

  void rejectOnboarding(String onboardingId, String reason);

  OnboardingData getOnboardingWithUserInfo(String onboardingId);

  void completeTokenV2(String onboardingId, MultipartFile contract);

  void completeOnboardingUsers(String onboardingId, MultipartFile contract);

  Resource getContract(String onboardingId);

  Resource getAttachment(String onboardingId, String filename);

  Resource getAggregatesCsv(String onboardingId, String productId);

  boolean verifyAllowedUserByRole(String onboardingId);
}
