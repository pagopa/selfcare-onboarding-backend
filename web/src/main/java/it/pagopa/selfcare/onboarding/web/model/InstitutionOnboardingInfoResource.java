package it.pagopa.selfcare.onboarding.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class InstitutionOnboardingInfoResource {

    @ApiModelProperty(value = "${swagger.onboarding.user.model.manager}")
    private UserResource manager;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.institutionData}")
    @NotNull
    private InstitutionData institution;

}
