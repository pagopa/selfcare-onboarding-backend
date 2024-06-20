package it.pagopa.selfcare.onboarding.web.model;

import it.pagopa.selfcare.onboarding.common.Origin;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.GeographicTaxonomy;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class AggregateInstitution {

    @NotNull(message = "taxCode is required")
    private String taxCode;

    @NotNull(message = "description is required")
    private String description;

    private String subunitCode;
    private String subunitType;
    private List<GeographicTaxonomy> geographicTaxonomies;
    private String address;
    private String zipCode;
    private String originId;
    private Origin origin;

}
