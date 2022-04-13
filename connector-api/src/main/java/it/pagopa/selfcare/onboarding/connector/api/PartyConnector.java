package it.pagopa.selfcare.onboarding.connector.api;

import it.pagopa.selfcare.onboarding.connector.model.RelationshipsResponse;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;

import java.util.Collection;

public interface PartyConnector {

    void onboardingOrganization(OnboardingData onboardingData);

    Collection<InstitutionInfo> getOnBoardedInstitutions();

    RelationshipsResponse getUserInstitutionRelationships(String institutionId, String productId);

    Collection<UserInfo> getUsers(String institutionId, UserInfo.UserInfoFilter userInfoFilter);

    Institution getInstitutionByExternalId(String institutionId);

    InstitutionInfo getOnboardedInstitution(String institutionId);

}
