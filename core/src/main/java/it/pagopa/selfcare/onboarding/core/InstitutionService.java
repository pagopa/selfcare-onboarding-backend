package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.connector.model.InstitutionOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;

import java.util.Collection;

public interface InstitutionService {

    void onboarding(OnboardingData onboardingData);

    Collection<InstitutionInfo> getInstitutions();

    InstitutionOnboardingData getInstitutionOnboardingData(String institutionId, String productId);

    Institution getInstitutionByExternalId(String institutionId);


}
