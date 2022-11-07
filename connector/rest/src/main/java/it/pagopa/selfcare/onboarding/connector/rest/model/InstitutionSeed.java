package it.pagopa.selfcare.onboarding.connector.rest.model;

import it.pagopa.selfcare.onboarding.connector.model.institutions.Attribute;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.DataProtectionOfficer;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionType;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PaymentServiceProvider;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class InstitutionSeed {

    public InstitutionSeed(OnboardingData onboardingData) {
        externalId = onboardingData.getInstitutionExternalId();
        description = onboardingData.getInstitutionUpdate().getDescription();
        digitalAddress = onboardingData.getInstitutionUpdate().getDigitalAddress();
        address = onboardingData.getInstitutionUpdate().getAddress();
        zipCode = onboardingData.getInstitutionUpdate().getZipCode();
        taxCode = onboardingData.getInstitutionUpdate().getTaxCode();
        institutionType = onboardingData.getInstitutionType();
        attributes = List.of();
        paymentServiceProvider = onboardingData.getInstitutionUpdate().getPaymentServiceProvider();
        dataProtectionOfficer = onboardingData.getInstitutionUpdate().getDataProtectionOfficer();
    }


    private String externalId;
    private String description;
    private String digitalAddress;
    private String address;
    private String zipCode;
    private String taxCode;
    private InstitutionType institutionType;
    private List<Attribute> attributes;
    private PaymentServiceProvider paymentServiceProvider;
    private DataProtectionOfficer dataProtectionOfficer;

}
