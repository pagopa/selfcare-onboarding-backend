package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.connector.api.OnboardingMsConnector;
import it.pagopa.selfcare.onboarding.connector.api.PartyConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    private final PartyConnector partyConnector;

    private final OnboardingMsConnector onboardingMsConnector;

    public TokenServiceImpl(PartyConnector partyConnector, OnboardingMsConnector onboardingMsConnector) {
        this.partyConnector = partyConnector;
        this.onboardingMsConnector = onboardingMsConnector;
    }

    @Override
    public void verifyToken(String tokenId) {
        log.trace("verifyToken start");
        log.debug("verifyToken id = {}", tokenId);
        Assert.notNull(tokenId, "TokenId is required");
        partyConnector.tokensVerify(tokenId);
        log.debug("verifyToken result = success");
        log.trace("verifyToken end");
    }

    @Override
    public void completeToken(String tokenId, MultipartFile contract) {
        log.trace("completeToken start");
        log.debug("completeToken id = {}", tokenId);
        Assert.notNull(tokenId, "TokenId is required");
        partyConnector.onboardingTokenComplete(tokenId, contract);
        log.debug("completeToken result = success");
        log.trace("completeToken end");
    }

    @Override
    public void completeTokenV2(String onboardingId, MultipartFile contract) {
        log.trace("completeTokenAsync start");
        log.debug("completeTokenAsync id = {}", onboardingId);
        Assert.notNull(onboardingId, "TokenId is required");
        onboardingMsConnector.onboardingTokenComplete(onboardingId, contract);
        log.debug("completeTokenAsync result = success");
        log.trace("completeTokenAsync end");
    }

    @Override
    public void deleteToken(String tokenId) {
        log.trace("deleteToken start");
        log.debug("deleteToken id = {}", tokenId);
        Assert.notNull(tokenId, "TokenId is required");
        partyConnector.deleteTokenComplete(tokenId);
        log.debug("deleteToken result = success");
        log.trace("deleteToken end");
    }
}
