package it.pagopa.selfcare.onboarding.connector.api;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingResource;

public interface PartyConnector {

    OnboardingResource onboardingOrganization(OnboardingData onboardingData);

}
