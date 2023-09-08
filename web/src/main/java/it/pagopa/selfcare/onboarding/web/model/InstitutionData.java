package it.pagopa.selfcare.onboarding.web.model;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionType;
import lombok.Data;

import javax.validation.Valid;

@Data
public class InstitutionData {


    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.id}")
    private String id;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.institutionType}")
    private InstitutionType institutionType;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.billingData}")
    private BillingDataDto billingData;

    @ApiModelProperty
    private String origin;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.companyInformations}")
    @Valid
    private CompanyInformationsResource companyInformations;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.assistance}")
    @Valid
    private AssistanceContactsResource assistanceContacts;
}
