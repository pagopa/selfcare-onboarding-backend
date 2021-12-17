package it.pagopa.selfcare.onboarding.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PartyRole;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class UserDto implements UserInfo {

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.id}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.id}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String surname;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.id}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String taxCode;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.id}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private PartyRole role;

    @ApiModelProperty(value = "${swagger.dashboard.institutions.model.id}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String email;

    @ApiModelProperty(hidden = true)
    private String productRole;

}
