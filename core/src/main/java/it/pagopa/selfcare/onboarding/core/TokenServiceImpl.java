package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.connector.api.OnboardingMsConnector;
import it.pagopa.selfcare.onboarding.connector.api.PartyConnector;
import it.pagopa.selfcare.onboarding.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.util.EnumSet;

@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    private final PartyConnector partyConnector;

    private final OnboardingMsConnector onboardingMsConnector;

    private final UserRegistryConnector userRegistryConnector;


    private static final EnumSet<User.Fields> USER_FIELD_LIST_ENHANCED = EnumSet.of(User.Fields.fiscalCode,
            User.Fields.name,
            User.Fields.familyName,
            User.Fields.workContacts);

    public TokenServiceImpl(PartyConnector partyConnector, OnboardingMsConnector onboardingMsConnector, UserRegistryConnector userRegistryConnector) {
        this.partyConnector = partyConnector;
        this.onboardingMsConnector = onboardingMsConnector;
        this.userRegistryConnector = userRegistryConnector;
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
    public void verifyOnboarding(String onboardingId) {
        log.trace("verifyOnboarding start");
        log.debug("verifyOnboarding id = {}", onboardingId);
        Assert.notNull(onboardingId, "OnboardingId is required");
        onboardingMsConnector.onboardingPending(onboardingId);
        log.debug("verifyOnboarding result = success");
        log.trace("verifyOnboarding end");
    }

    @Override
    public OnboardingData getOnboardingWithUserInfo(String onboardingId) {
        log.trace("getOnboardingWithUserInfo start");
        log.debug("getOnboardingWithUserInfo id = {}", onboardingId);
        Assert.notNull(onboardingId, "OnboardingId is required");
        OnboardingData onboardingData = onboardingMsConnector.getOnboardingWithUserInfo(onboardingId);
        log.debug("getOnboardingWithUserInfo result = success");
        log.trace("getOnboardingWithUserInfo end");
        return onboardingData;
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
