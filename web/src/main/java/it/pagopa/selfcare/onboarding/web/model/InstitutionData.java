package it.pagopa.selfcare.onboarding.web.model;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import lombok.Data;

import javax.validation.Valid;

@Data
public class InstitutionData {


    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.id}")
    private String id;
    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.institutionType}")
    private InstitutionType institutionType;
    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.billingData}")
    private BillingDataResponseDto billingData;
    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.city}")
    private String city;
    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.county}")
    private String county;
    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.country}")
    private String country;
    @ApiModelProperty
    private String origin;
    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.companyInformations}")
    @Valid
    private CompanyInformationsResource companyInformations;
    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.assistance}")
    @Valid
    private AssistanceContactsResource assistanceContacts;
}
