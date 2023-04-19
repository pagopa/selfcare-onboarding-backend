package it.pagopa.selfcare.onboarding.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PnPGOnboardingDto {

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.users}", required = true)
    @NotEmpty
    @Valid
    private List<PnPGUserDto> users;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.billingData}", required = true)
    @NotNull
    @Valid
    private PnPGBillingDataDto billingData;

}
