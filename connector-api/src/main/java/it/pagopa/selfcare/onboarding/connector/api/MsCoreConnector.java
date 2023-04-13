package it.pagopa.selfcare.onboarding.connector.api;

import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.CreatePnPGInstitutionData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PnPGOnboardingData;

public interface MsCoreConnector {

    void onboardingPGOrganization(PnPGOnboardingData onboardingData);

    Institution getInstitutionByExternalId(String externalInstitutionId);

    Institution createPGInstitutionUsingExternalId(CreatePnPGInstitutionData createPnPGData);

    void verifyOnboarding(String externalInstitutionId, String productId);

}