package it.pagopa.selfcare.onboarding.web.model;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionType;
import lombok.Data;

@Data
public class Institution {

    @ApiModelProperty(value = "")
    private InstitutionType organizationType;

    @ApiModelProperty(value = "")
    private BillingDataDto billingData;
}
