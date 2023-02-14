package it.pagopa.selfcare.onboarding.connector.api;

import it.pagopa.selfcare.onboarding.connector.model.PnPGInstitutionLegalAddressData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionPnPGInfo;
import it.pagopa.selfcare.onboarding.connector.model.institutions.PnPGMatchInfo;

public interface PartyRegistryProxyConnector {

    InstitutionPnPGInfo getInstitutionsByUserFiscalCode(String taxCode);

    PnPGMatchInfo matchInstitutionAndUser(String matchInstitutionAndUser, String userTaxCode);

    PnPGInstitutionLegalAddressData getInstitutionLegalAddress(String externalInstitutionId);

}
