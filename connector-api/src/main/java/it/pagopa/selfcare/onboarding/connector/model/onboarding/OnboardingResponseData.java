package it.pagopa.selfcare.onboarding.connector.model.onboarding;

import it.pagopa.selfcare.onboarding.connector.model.RelationshipState;
import lombok.Data;

@Data
public class OnboardingResponseData {
    private String institutionId;
    private String description;
    private String address;
    private String taxCode;
    private String zipCode;
    private String digitalAddress;
    private RelationshipState state;
    private PartyRole role;
}
