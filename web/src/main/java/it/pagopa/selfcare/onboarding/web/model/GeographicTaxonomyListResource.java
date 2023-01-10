package it.pagopa.selfcare.onboarding.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class GeographicTaxonomyListResource {
    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.geographicTaxonomy}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private List<GeographicTaxonomyResource> geographicTaxonomies;
}