package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;

import java.util.Collection;

public interface InstitutionService {

    void onboarding(OnboardingData onboardingData);

    Collection<InstitutionInfo> getInstitutions();

    UserInfo getManager(String institutionId, String productId);

}
