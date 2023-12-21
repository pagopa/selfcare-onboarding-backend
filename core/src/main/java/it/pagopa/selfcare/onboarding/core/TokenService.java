package it.pagopa.selfcare.onboarding.core;

import org.springframework.web.multipart.MultipartFile;

public interface TokenService {

    public void verifyToken(String tokenId);

    void verifyOnboarding(String onboardingId);

    public void completeToken(String tokenId, MultipartFile contract);

    void completeTokenV2(String onboardingId, MultipartFile contract);

    void deleteToken(String tokenId);
}
