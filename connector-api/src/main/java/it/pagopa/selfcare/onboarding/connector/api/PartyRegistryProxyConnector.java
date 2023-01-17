package it.pagopa.selfcare.onboarding.connector.api;

import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionPnPGInfo;

public interface PartyRegistryProxyConnector {

    InstitutionPnPGInfo getInstitutionsByUserFiscalCode(String fiscalCode);

}
