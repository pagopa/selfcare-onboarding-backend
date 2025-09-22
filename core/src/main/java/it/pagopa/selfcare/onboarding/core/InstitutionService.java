package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.connector.model.InstitutionLegalAddressData;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.RecipientCodeStatusResult;
import it.pagopa.selfcare.onboarding.connector.model.institutions.*;
import it.pagopa.selfcare.onboarding.connector.model.institutions.infocamere.InstitutionInfoIC;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.GeographicTaxonomy;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface InstitutionService {

    void onboardingProductV2(OnboardingData onboardingData);

    void onboardingCompanyV2(OnboardingData onboardingData, String userFiscalCode);

    void onboardingProduct(OnboardingData onboardingData);

    void onboardingPaAggregator(OnboardingData entity);

    List<InstitutionInfo> getInstitutions(String productId, String userId);

    List<Institution> getActiveOnboarding(String taxCode,String productId,String subunitCode);

    InstitutionOnboardingData getInstitutionOnboardingDataById(String institutionId, String productId);

    InstitutionOnboardingData getInstitutionOnboardingData(String externalInstitutionId, String productId);

    List<GeographicTaxonomy> getGeographicTaxonomyList(String externalInstitutionId);

    Institution getInstitutionByExternalId(String externalInstitutionId);

    List<GeographicTaxonomy> getGeographicTaxonomyList(String taxCode, String subunitCode);

    void verifyOnboarding(String externalInstitutionId, String productId);

    void verifyOnboarding(String productId, String taxCode, String origin, String originId, String subunitCode, Boolean soleTrader);

    void checkOrganization(String productId, String fiscalCode, String vatNumber);
    MatchInfoResult matchInstitutionAndUser(String externalInstitutionId, User user);

    InstitutionLegalAddressData getInstitutionLegalAddress(String externalInstitutionId);

    InstitutionInfoIC getInstitutionsByUser(String taxCode);

    List<Institution> getByFilters(String productId, String taxCode, String origin, String originId, String subunitCode);

    VerifyAggregateResult validateAggregatesCsv(MultipartFile file, String productId);

    RecipientCodeStatusResult checkRecipientCode(String originId, String recipientCode);

    void onboardingUsersPgFromIcAndAde(OnboardingData onboardingUserPgRequest);

    ManagerVerification verifyManager(String taxCode, String companyTaxCode);
}
