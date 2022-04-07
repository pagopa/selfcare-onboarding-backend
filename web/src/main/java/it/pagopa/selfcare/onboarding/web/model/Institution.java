package it.pagopa.selfcare.onboarding.web.model;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OrganizationType;
import lombok.Data;

@Data
public class Institution {

    @ApiModelProperty(value = "")
    private OrganizationType organizationType;

    @ApiModelProperty(value = "")
    private BillingDataDto billingData;
}
