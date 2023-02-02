package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionPnPGInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PnPGOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;

public interface PnPGInstitutionService {

    InstitutionPnPGInfo getInstitutionsByUser(User user);

    void onboarding(PnPGOnboardingData onboardingData);

}
