package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionPnPGInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;

public interface PnPGInstitutionService {

    InstitutionPnPGInfo getInstitutionsByUserId(String userId);

    void onboarding(OnboardingData onboardingData);

}
