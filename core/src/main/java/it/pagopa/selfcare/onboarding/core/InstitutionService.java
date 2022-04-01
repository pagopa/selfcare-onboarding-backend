package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.connector.model.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingResource;

import java.util.Collection;

public interface InstitutionService {

    OnboardingResource onboarding(OnboardingData onboardingData);

    Collection<InstitutionInfo> getInstitutions();

}
