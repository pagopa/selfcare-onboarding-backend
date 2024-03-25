package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.connector.api.OnboardingMsConnector;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    private final OnboardingMsConnector onboardingMsConnector;
    private static final String ONBOARDING_ID_REQUIRED_MESSAGE = "OnboardingId is required";

    public TokenServiceImpl(OnboardingMsConnector onboardingMsConnector) {
        this.onboardingMsConnector = onboardingMsConnector;
    }

    @Override
    public OnboardingData verifyOnboarding(String onboardingId) {
        log.trace("verifyOnboarding start");
        log.debug("verifyOnboarding id = {}", onboardingId);
        Assert.notNull(onboardingId, ONBOARDING_ID_REQUIRED_MESSAGE);
        OnboardingData onboardingData = onboardingMsConnector.getOnboarding(onboardingId);
        log.debug("verifyOnboarding result = success");
        log.trace("verifyOnboarding end");
        return onboardingData;
    }

    @Override
    public void approveOnboarding(String onboardingId) {
        log.trace("approveOnboarding start");
        log.debug("approveOnboarding id = {}", onboardingId);
        Assert.notNull(onboardingId, ONBOARDING_ID_REQUIRED_MESSAGE);
        onboardingMsConnector.approveOnboarding(onboardingId);
        log.debug("approveOnboarding result = success");
        log.trace("approveOnboarding end");
    }

    @Override
    public void rejectOnboarding(String onboardingId, String reason) {
        log.trace("rejectOnboarding start");
        log.debug("rejectOnboarding id = {}", onboardingId);
        Assert.notNull(onboardingId, ONBOARDING_ID_REQUIRED_MESSAGE);
        onboardingMsConnector.rejectOnboarding(onboardingId, reason);
        log.debug("rejectOnboarding result = success");
        log.trace("rejectOnboarding end");
    }

    @Override
    public OnboardingData getOnboardingWithUserInfo(String onboardingId) {
        log.trace("getOnboardingWithUserInfo start");
        log.debug("getOnboardingWithUserInfo id = {}", onboardingId);
        Assert.notNull(onboardingId, ONBOARDING_ID_REQUIRED_MESSAGE);
        OnboardingData onboardingData = onboardingMsConnector.getOnboardingWithUserInfo(onboardingId);
        log.debug("getOnboardingWithUserInfo result = success");
        log.trace("getOnboardingWithUserInfo end");
        return onboardingData;
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
    public Resource getContract(String onboardingId) {
        log.trace("getContract start");
        log.debug("getContract id = {}", onboardingId);
        Assert.notNull(onboardingId, "TokenId is required");
        Resource resource = onboardingMsConnector.getContract(onboardingId);
        log.debug("getContract result = success");
        log.trace("getContract end");
        return resource;
    }
}
