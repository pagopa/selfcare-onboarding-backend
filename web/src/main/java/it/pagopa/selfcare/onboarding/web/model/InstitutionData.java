package it.pagopa.selfcare.onboarding.web.model;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionType;
import lombok.Data;

@Data
public class InstitutionData {

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.institutionType}")
    private InstitutionType institutionType;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.billingData}")
    private BillingDataDto billingData;

    @ApiModelProperty
    private String origin;
}
