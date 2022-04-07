package it.pagopa.selfcare.onboarding.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class InstitutionResource {

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.name}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String description;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.institutionId}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String institutionId;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.digitalAddress}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String digitalAddress;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.address}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String address;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.zipCode}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String zipCode;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.taxCode}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String taxCode;

}
