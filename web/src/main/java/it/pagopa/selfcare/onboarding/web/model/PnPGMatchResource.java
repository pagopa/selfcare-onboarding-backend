package it.pagopa.selfcare.onboarding.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PnPGMatchResource {


    @ApiModelProperty(value = "${swagger.onboarding.pnPGInstitutions.model.matchResult}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private boolean matchResult;

}
