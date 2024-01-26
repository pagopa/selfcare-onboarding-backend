package it.pagopa.selfcare.onboarding.connector.model.onboarding;

import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import lombok.Data;

import java.util.List;


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
    private List<GeographicTaxonomy> geographicTaxonomies;
    private String rea;
    private String shareCapital;
    private String businessRegisterPlace;
    private String supportEmail;
    private String supportPhone;
    private Boolean imported;
    private AdditionalInformations additionalInformations;

}
