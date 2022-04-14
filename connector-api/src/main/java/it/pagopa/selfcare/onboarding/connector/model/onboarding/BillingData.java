package it.pagopa.selfcare.onboarding.connector.model.onboarding;

import lombok.Data;

@Data
public class BillingData {
    private String vatNumber;
    private String recipientCode;
    private boolean publicService;
}
