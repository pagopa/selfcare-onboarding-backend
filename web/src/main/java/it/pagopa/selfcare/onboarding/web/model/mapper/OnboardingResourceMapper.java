package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.web.model.OnboardingProductDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OnboardingResourceMapper {

    @Mapping(source = "billingData", target = "billing")
    @Mapping(source = "billingData.businessName", target = "institutionUpdate.description")
    @Mapping(source = "billingData.registeredOffice", target = "institutionUpdate.address")
    @Mapping(source = "pspData", target = "institutionUpdate.paymentServiceProvider")
    @Mapping(source = "pspData", target = "institutionUpdate.dataProtectionOfficer")
    @Mapping(source = "geographicTaxonomies", target = "institutionUpdate.geographicTaxonomies")
    @Mapping(source = "companyInformations.rea", target = "institutionUpdate.rea")
    @Mapping(source = "companyInformations.shareCapital", target = "institutionUpdate.shareCapital")
    @Mapping(source = "companyInformations.businessRegisterPlace", target = "institutionUpdate.businessRegisterPlace")
    @Mapping(source = "assistanceContacts.supportEmail", target = "institutionUpdate.supportEmail")
    @Mapping(source = "assistanceContacts.supportPhone", target = "institutionUpdate.supportPhone")
    OnboardingData toEntity(OnboardingProductDto dto);


}
