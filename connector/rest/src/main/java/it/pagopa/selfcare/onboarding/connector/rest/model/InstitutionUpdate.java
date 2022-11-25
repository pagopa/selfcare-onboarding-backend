package it.pagopa.selfcare.onboarding.connector.rest.model;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.DataProtectionOfficer;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionType;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PaymentServiceProvider;
import lombok.Data;

@Data
public class InstitutionUpdate {

    private InstitutionType institutionType;
    private String description;
    private String digitalAddress;
    private String address;
    private String taxCode;
    private String zipCode;
    private PaymentServiceProvider paymentServiceProvider;
    private DataProtectionOfficer dataProtectionOfficer;

}
