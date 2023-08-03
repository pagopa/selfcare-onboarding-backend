package it.pagopa.selfcare.onboarding.web.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class VerificationLegalAddressRequest {

    @NotBlank
    private String taxCode;
}
