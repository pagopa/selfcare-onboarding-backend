package it.pagopa.selfcare.onboarding.web.model;

import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.onboarding.common.InstitutionType;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class OnboardingProductDto {

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.users}", required = true)
    @NotEmpty
    @Valid
    private List<UserDto> users;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.billingData}")
    @Valid
    private BillingDataDto billingData;

    @ApiModelProperty(value = "${swagger.onboarding.institution.model.locationData}")
    private InstitutionLocationDataDto institutionLocationData;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.institutionType}", required = true)
    @NotNull
    private InstitutionType institutionType;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.origin}")
    private String origin;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.originId}")
    private String originId;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.pricingPlan}")
    private String pricingPlan;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.pspData}")
    @Valid
    private PspDataDto pspData;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.geographicTaxonomies}")
    @Valid
    private List<GeographicTaxonomyDto> geographicTaxonomies;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.companyInformations}")
    @Valid
    private CompanyInformationsDto companyInformations;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.assistance}")
    @Valid
    private AssistanceContactsDto assistanceContacts;

    @ApiModelProperty(value = "${swagger.onboarding.product.model.id}", required = true)
    @NotNull
    private String productId;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.taxCode}")
    private String taxCode;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.subunitCode}")
    private String subunitCode;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.subunitType}")
    private String subunitType;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.additionalInformations}")
    private AdditionalInformationsDto additionalInformations;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.isAggregator}")
    private Boolean isAggregator;

    @ApiModelProperty(value = "${swagger.onboarding.institutions.model.aggregates}")
    private List<AggregateInstitution> aggregates;
}
