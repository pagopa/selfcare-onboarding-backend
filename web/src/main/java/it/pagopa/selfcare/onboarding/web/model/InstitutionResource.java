package it.pagopa.selfcare.onboarding.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Data
public class InstitutionResource {

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.id}", required = true)
    private UUID id;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.name}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String description;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.externalId}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String externalId;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.originId}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String originId;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.institutionType}", required = true)
    private InstitutionType institutionType;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.digitalAddress}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String digitalAddress;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.address}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String address;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.zipCode}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String zipCode;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.taxCode}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String taxCode;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.origin}", required = true)
    private String origin;

}
