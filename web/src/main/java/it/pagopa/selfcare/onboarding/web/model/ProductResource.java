package it.pagopa.selfcare.onboarding.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.onboarding.connector.model.product.ProductStatus;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ProductResource {

    @ApiModelProperty(value = "${swagger.onboarding.product.model.id}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String id;

    @ApiModelProperty(value = "${swagger.onboarding.product.model.title}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String title;

    @ApiModelProperty(value = "${swagger.onboarding.product.model.parentId}", required = false)
    private String parentId;

    @ApiModelProperty(value = "${swagger.onboarding.product.model.status}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private ProductStatus status;
}
