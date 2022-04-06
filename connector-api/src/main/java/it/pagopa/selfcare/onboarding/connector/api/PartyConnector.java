package it.pagopa.selfcare.onboarding.connector.api;

import it.pagopa.selfcare.onboarding.connector.model.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipsResponse;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingResource;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PartyRole;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;

import java.util.Collection;

public interface PartyConnector {

    OnboardingResource onboardingOrganization(OnboardingData onboardingData);

    Collection<InstitutionInfo> getOnBoardedInstitutions();

    RelationshipsResponse getUserInstitutionRelationships(String institutionId, String productId);

    UserInfo getUser(String institutionId, String productId, PartyRole role);

}
