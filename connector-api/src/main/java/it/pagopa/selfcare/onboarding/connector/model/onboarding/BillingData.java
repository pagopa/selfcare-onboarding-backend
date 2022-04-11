package it.pagopa.selfcare.onboarding.connector.model.onboarding;

import lombok.Data;

@Data
public class BillingData {
    //TODO to be confirmed don't do any test on this
    private String description;
    private String physicalAddress;
    private String digitalAddress;
    private String taxCode;
    private String vatNumber;
    private String recipientCode;
    private boolean publicService;
}
