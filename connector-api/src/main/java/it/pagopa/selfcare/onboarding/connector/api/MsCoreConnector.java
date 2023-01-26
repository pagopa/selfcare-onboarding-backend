package it.pagopa.selfcare.onboarding.connector.api;

import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;

public interface MsCoreConnector {

    void onboardingOrganization(OnboardingData onboardingData);

    Institution getInstitutionByExternalId(String externalInstitutionId);

    Institution createPGInstitutionUsingExternalId(String institutionExternalId);

    void verifyOnboarding(String externalInstitutionId, String productId);

}
