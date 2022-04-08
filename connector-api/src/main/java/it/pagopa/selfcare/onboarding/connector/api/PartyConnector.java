package it.pagopa.selfcare.onboarding.connector.api;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;

public interface PartyConnector {

    void onboardingOrganization(OnboardingData onboardingData);

}
