package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.connector.model.PnPGInstitutionLegalAddressData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionPnPGInfo;
import it.pagopa.selfcare.onboarding.connector.model.institutions.PnPGMatchInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PnPGOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;

public interface PnPGInstitutionService {

    InstitutionPnPGInfo getInstitutionsByUser(User user);

    PnPGMatchInfo matchInstitutionAndUser(String externalInstitutionId, User user);

    PnPGInstitutionLegalAddressData getInstitutionLegalAddress(String externalInstitutionId);

    void onboarding(PnPGOnboardingData onboardingData);

}
