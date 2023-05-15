package it.pagopa.selfcare.onboarding.connector.api;

import it.pagopa.selfcare.onboarding.connector.model.InstitutionLegalAddressData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionPnPGInfo;
import it.pagopa.selfcare.onboarding.connector.model.institutions.MatchInfoResult;

public interface PartyRegistryProxyConnector {

    InstitutionPnPGInfo getInstitutionsByUserFiscalCode(String taxCode);

    MatchInfoResult matchInstitutionAndUser(String matchInstitutionAndUser, String userTaxCode);

    InstitutionLegalAddressData getInstitutionLegalAddress(String externalInstitutionId);

}
