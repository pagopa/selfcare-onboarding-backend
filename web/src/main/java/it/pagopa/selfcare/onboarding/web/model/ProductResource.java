package it.pagopa.selfcare.onboarding.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;

public class ProductResource {
    @ApiModelProperty(value = "${swagger.onboarding.product.model.id}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String id;
}
