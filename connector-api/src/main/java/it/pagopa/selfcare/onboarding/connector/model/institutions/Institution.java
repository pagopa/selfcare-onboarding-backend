package it.pagopa.selfcare.onboarding.connector.model.institutions;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.DataProtectionOfficer;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionType;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PaymentServiceProvider;
import lombok.Data;

import java.util.List;

@Data
public class Institution {

    private String id;
    private String externalId;
    private String originId;
    private String description;
    private String digitalAddress;
    private String address;
    private String zipCode;
    private String taxCode;
    private String origin;
    private InstitutionType institutionType;
    private List<Attribute> attributes;
    private PaymentServiceProvider paymentServiceProvider;
    private DataProtectionOfficer dataProtectionOfficer;

}
