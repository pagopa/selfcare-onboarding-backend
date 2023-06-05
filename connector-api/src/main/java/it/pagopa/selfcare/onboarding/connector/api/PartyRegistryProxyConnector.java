package it.pagopa.selfcare.onboarding.connector.api;

import it.pagopa.selfcare.onboarding.connector.model.InstitutionLegalAddressData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.MatchInfoResult;
import it.pagopa.selfcare.onboarding.connector.model.institutions.infocamere.InstitutionInfoIC;

public interface PartyRegistryProxyConnector {

    InstitutionInfoIC getInstitutionsByUserFiscalCode(String taxCode);

    MatchInfoResult matchInstitutionAndUser(String matchInstitutionAndUser, String userTaxCode);

    InstitutionLegalAddressData getInstitutionLegalAddress(String externalInstitutionId);

}
