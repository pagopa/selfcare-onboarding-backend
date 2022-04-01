package it.pagopa.selfcare.onboarding.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class InstitutionResource {

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.id}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String id;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.name}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String name;


}
