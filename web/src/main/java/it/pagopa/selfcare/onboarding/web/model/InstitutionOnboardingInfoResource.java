package it.pagopa.selfcare.onboarding.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class InstitutionOnboardingInfoResource {

    @ApiModelProperty(value = "${swagger.onboarding.user.model.manager}")
    private UserDto manager;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.institution}")
    @NotNull
    private InstitutionResource institution;

}
