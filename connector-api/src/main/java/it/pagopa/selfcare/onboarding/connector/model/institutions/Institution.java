package it.pagopa.selfcare.onboarding.connector.model.institutions;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionType;
import lombok.Data;

@Data
public class Institution {

    private String id;
    private String institutionId;
    private String description;
    private String digitalAddress;
    private String address;
    private String zipCode;
    private String taxCode;
    private InstitutionType type;
    private String origin;
    private Attributes attributes;

}
