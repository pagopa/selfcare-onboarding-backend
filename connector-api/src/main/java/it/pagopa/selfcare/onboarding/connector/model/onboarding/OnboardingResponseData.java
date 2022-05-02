package it.pagopa.selfcare.onboarding.connector.model.onboarding;

import it.pagopa.selfcare.onboarding.connector.model.RelationshipState;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Attributes;
import lombok.Data;

import java.util.List;

@Data
public class OnboardingResponseData {
    private String id;
    private String institutionId;
    private String description;
    private String taxCode;
    private String address;
    private String digitalAddress;
    private String zipCode;
    private RelationshipState state;
    private PartyRole role;
    private ProductInfo productInfo;
    private InstitutionType institutionType;
    private String pricingPlan;
    private BillingData billing;
    private String origin;
    private List<Attributes> attributes;
}
