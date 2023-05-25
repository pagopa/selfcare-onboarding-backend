package it.pagopa.selfcare.onboarding.connector.rest.model;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class InstitutionFromIpaPost {

    @NotNull
    private String taxCode;
    private String subunitCode;
    private String subunitType;
}
