package it.pagopa.selfcare.onboarding.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class MatchInfoResultResource {


    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.matchResult}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private boolean verificationResult;

}
