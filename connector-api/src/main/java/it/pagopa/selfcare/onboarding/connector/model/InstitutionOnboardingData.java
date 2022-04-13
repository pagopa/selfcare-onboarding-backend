package it.pagopa.selfcare.onboarding.connector.model;

import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
import lombok.Data;

@Data
public class InstitutionOnboardingData {
    private UserInfo manager;
    private InstitutionInfo institution;
}
