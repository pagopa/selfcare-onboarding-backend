package it.pagopa.selfcare.onboarding.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ReasonForRejectDto {

    @ApiModelProperty(value = "${swagger.onboarding.institution.model.reason}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String reason;
}
