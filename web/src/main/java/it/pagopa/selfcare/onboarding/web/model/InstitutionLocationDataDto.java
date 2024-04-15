package it.pagopa.selfcare.onboarding.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class InstitutionLocationDataDto {
    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.city}")
    @JsonProperty
    private String city;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.county}")
    @JsonProperty
    private String county;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.country}")
    @JsonProperty
    private String country;

}
