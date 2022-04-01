package it.pagopa.selfcare.onboarding.web.model;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.BillingData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class OnboardingDto {

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.users}", required = true)
    @NotEmpty
    @Valid
    List<UserDto> users;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.billingData}", required = false)
    @Valid
    BillingData billingData;
}
