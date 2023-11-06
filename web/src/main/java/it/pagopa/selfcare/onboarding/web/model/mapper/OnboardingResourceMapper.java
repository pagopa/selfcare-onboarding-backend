package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.web.model.CompanyOnboardingDto;
import it.pagopa.selfcare.onboarding.web.model.OnboardingProductDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;


@Mapper(componentModel = "spring")
public interface OnboardingResourceMapper {

    @Mapping(source = "billingData", target = "billing")
    @Mapping(source = "institutionLocationData", target = "location")
    @Mapping(source = "billingData.businessName", target = "institutionUpdate.description")
    @Mapping(source = "billingData.registeredOffice", target = "institutionUpdate.address")
    @Mapping(source = "pspData", target = "institutionUpdate.paymentServiceProvider")
    @Mapping(source = "pspData.dpoData", target = "institutionUpdate.dataProtectionOfficer")
    @Mapping(source = "geographicTaxonomies", target = "institutionUpdate.geographicTaxonomies")
    @Mapping(source = "companyInformations.rea", target = "institutionUpdate.rea")
    @Mapping(source = "companyInformations.shareCapital", target = "institutionUpdate.shareCapital")
    @Mapping(source = "companyInformations.businessRegisterPlace", target = "institutionUpdate.businessRegisterPlace")
    @Mapping(source = "assistanceContacts.supportEmail", target = "institutionUpdate.supportEmail")
    @Mapping(source = "assistanceContacts.supportPhone", target = "institutionUpdate.supportPhone")
    OnboardingData toEntity(OnboardingProductDto dto);

    @Mapping(source = "billingData", target = "billing")
    @Mapping(source = "billingData.businessName", target = "institutionUpdate.description")
    @Mapping(source = "billingData.taxCode", target = "institutionUpdate.taxCode")
    @Mapping(source = "billingData.digitalAddress", target = "institutionUpdate.digitalAddress")
    @Mapping(target = "origin", expression = "java(getOrigin(dto.getBillingData().isCertified()))")
    OnboardingData toEntity(CompanyOnboardingDto dto);

    @Named("getOrigin")
    default String getOrigin(Boolean certified) {
        return Boolean.TRUE.equals(certified) ? "INFOCAMERE" : "ADE";
    }

}
