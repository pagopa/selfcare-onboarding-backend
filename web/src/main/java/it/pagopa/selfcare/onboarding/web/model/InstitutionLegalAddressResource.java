package it.pagopa.selfcare.onboarding.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class InstitutionLegalAddressResource {

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.address}", required = true)
    @JsonProperty(required = true)
    private String address;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.zipCode}", required = true)
    @JsonProperty(required = true)
    private String zipCode;

}
