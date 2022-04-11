package it.pagopa.selfcare.onboarding.web.model;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OrganizationType;
import lombok.Data;

@Data
public class InstitutionData {

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.organizationType}")
    private OrganizationType organizationType;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.billingData}")
    private BillingDataDto billingData;

    @ApiModelProperty
    private String origin;
}
