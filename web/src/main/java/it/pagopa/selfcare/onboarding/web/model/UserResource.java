package it.pagopa.selfcare.onboarding.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PartyRole;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class UserResource {

    @ApiModelProperty(value = "${swagger.onboarding.user.model.id}", required = true)
    @NotNull
    private UUID id;

    @ApiModelProperty(value = "${swagger.onboarding.user.model.name}")
    private String name;

    @ApiModelProperty(value = "${swagger.onboarding.user.model.surname}")
    private String surname;

    @ApiModelProperty(value = "${swagger.onboarding.user.model.institutionalEmail}")
    @Email
    private String email;

    @ApiModelProperty(value = "${swagger.onboarding.user.model.fiscalCode}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String taxCode;

    @ApiModelProperty(value = "${swagger.onboarding.user.model.role}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private PartyRole role;


    @ApiModelProperty(value = "${swagger.onboarding.user.model.status}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String status;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.id}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private UUID institutionId;

}
