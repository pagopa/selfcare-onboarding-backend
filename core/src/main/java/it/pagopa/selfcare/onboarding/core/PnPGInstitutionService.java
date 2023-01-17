package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionPnPGInfo;

public interface PnPGInstitutionService {

    InstitutionPnPGInfo getInstitutionsByUserId(String userId);

}
