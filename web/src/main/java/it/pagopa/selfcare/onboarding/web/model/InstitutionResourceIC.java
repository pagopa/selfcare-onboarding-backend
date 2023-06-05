package it.pagopa.selfcare.onboarding.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class InstitutionResourceIC {


    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.legalTaxId}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String legalTaxId;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.requestDateTime}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String requestDateTime;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.businesses}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private List<BusinessResourceIC> businesses;

}
