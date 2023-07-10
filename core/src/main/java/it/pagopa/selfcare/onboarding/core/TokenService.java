package it.pagopa.selfcare.onboarding.core;

import org.springframework.web.multipart.MultipartFile;

public interface TokenService {

    public void verifyToken(String tokenId);
    public void completeToken(String tokenId, MultipartFile contract);

    void deleteToken(String tokenId);
}
