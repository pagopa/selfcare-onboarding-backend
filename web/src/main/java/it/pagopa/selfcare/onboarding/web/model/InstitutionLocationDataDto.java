package it.pagopa.selfcare.onboarding.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class InstitutionLocationDataDto {
    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.city}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String city;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.county}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String county;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.country}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String country;

}
