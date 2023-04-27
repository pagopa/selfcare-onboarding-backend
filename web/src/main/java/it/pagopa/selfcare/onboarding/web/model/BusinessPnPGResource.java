package it.pagopa.selfcare.onboarding.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class BusinessPnPGResource {

    @ApiModelProperty(value = "${swagger.onboarding.pnPGInstitutions.businessPnpg.model.businessName}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String businessName;

    @ApiModelProperty(value = "${swagger.onboarding.pnPGInstitutions.businessPnpg.model.businessTaxId}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String businessTaxId;

}
