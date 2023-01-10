package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.connector.model.InstitutionOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.GeographicTaxonomy;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;

import java.util.Collection;
import java.util.List;

public interface InstitutionService {

    void onboarding(OnboardingData onboardingData);

    Collection<InstitutionInfo> getInstitutions();

    InstitutionOnboardingData getInstitutionOnboardingData(String externalInstitutionId, String productId);

    List<GeographicTaxonomy> getGeographicTaxonomyList(String externalInstitutionId);

    Institution getInstitutionByExternalId(String externalInstitutionId);

    void verifyOnboarding(String externalInstitutionId, String productId);

}
