package it.pagopa.selfcare.onboarding.connector.api;

import it.pagopa.selfcare.onboarding.connector.model.RelationshipsResponse;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.institutions.OnboardingResource;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

public interface PartyConnector {

    void onboarding(OnboardingData onboardingData);

    void onboardingOrganization(OnboardingData onboardingData);

    Collection<InstitutionInfo> getOnBoardedInstitutions(String productFilter);

    RelationshipsResponse getUserInstitutionRelationships(String externalInstitutionId, UserInfo.UserInfoFilter userInfoFilter);

    Collection<UserInfo> getUsers(String externalInstitutionId, UserInfo.UserInfoFilter userInfoFilter);

    List<Institution> getInstitutionsByTaxCodeAndSubunitCode(String taxCode, String subunitCode);

    Institution getInstitutionByExternalId(String externalInstitutionId);

    List<OnboardingResource> getOnboardings(String institutionId, String productId);

    Institution createInstitutionFromIpa(String taxCode, String subunitCode, String subunitType);

    Institution createInstitutionFromANAC(OnboardingData onboardingData);

    Institution createInstitutionUsingExternalId(String institutionExternalId);

    Institution createInstitution(OnboardingData onboardingData);

    UserInfo getInstitutionManager(String externalInstitutionId, String productId);

    InstitutionInfo getInstitutionBillingData(String externalId, String productId);

    void verifyOnboarding(String externalInstitutionId, String productId);

    void verifyOnboarding(String taxCode, String subunitCode, String productId);

    void tokensVerify(String tokenId);

    void onboardingTokenComplete(String tokenId, MultipartFile contract);

    void deleteTokenComplete(String tokenId);
}
