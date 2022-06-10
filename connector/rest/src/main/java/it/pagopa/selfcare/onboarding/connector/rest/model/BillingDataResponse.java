package it.pagopa.selfcare.onboarding.connector.rest.model;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.Billing;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionType;
import lombok.Data;

@Data
public class BillingDataResponse {

    private String institutionId;
    private String externalId;
    private String origin;
    private String originId;
    private String description;
    private String taxCode;
    private String digitalAddress;
    private String address;
    private String zipCode;
    private InstitutionType institutionType;
    private String pricingPlan;
    private Billing billing;

}
