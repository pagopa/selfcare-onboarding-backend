package it.pagopa.selfcare.onboarding.connector.rest.mapper;


import it.pagopa.selfcare.onboarding.connector.model.onboarding.DataProtectionOfficer;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.GeographicTaxonomy;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PaymentServiceProvider;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Optional;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface OnboardingMapper {

    @Mapping(target = "institution", source = ".", qualifiedByName = "toInstitutionBase")
    OnboardingPaRequest toOnboardingPaRequest(OnboardingData onboardingData);
    @Mapping(target = "institution", source = ".", qualifiedByName = "toInstitutionPsp")
    OnboardingPspRequest toOnboardingPspRequest(OnboardingData onboardingData);
    @Mapping(target = "institution", source = ".", qualifiedByName = "toInstitutionBase")
    OnboardingDefaultRequest toOnboardingDefaultRequest(OnboardingData onboardingData);


    @Named("toInstitutionBase")
    default InstitutionBaseRequest toInstitutionBase(OnboardingData onboardingData) {
        InstitutionBaseRequest institution = new InstitutionBaseRequest();
        institution.institutionType(InstitutionType.valueOf(onboardingData.getInstitutionType().name()));
        institution.taxCode(onboardingData.getTaxCode());
        institution.subunitCode(onboardingData.getSubunitCode());
        institution.subunitType(InstitutionPaSubunitType.fromValue(onboardingData.getSubunitType()));
        institution.digitalAddress(onboardingData.getInstitutionUpdate().getDigitalAddress());
        institution.address(onboardingData.getInstitutionUpdate().getAddress());
        institution.zipCode(onboardingData.getInstitutionUpdate().getZipCode());
        institution.geographicTaxonomyCodes(onboardingData.getInstitutionUpdate().getGeographicTaxonomies().stream()
                        .map(GeographicTaxonomy::getCode)
                        .collect(Collectors.toList()));
        institution.rea(onboardingData.getInstitutionUpdate().getRea());
        institution.shareCapital(onboardingData.getInstitutionUpdate().getShareCapital());
        institution.businessRegisterPlace(onboardingData.getInstitutionUpdate().getBusinessRegisterPlace());
        institution.supportEmail(onboardingData.getInstitutionUpdate().getSupportEmail());
        institution.supportPhone(onboardingData.getInstitutionUpdate().getSupportPhone());
        institution.imported(onboardingData.getInstitutionUpdate().getImported());
        return institution;
    }


    @Named("toInstitutionPsp")
    default InstitutionPspRequest toInstitutionPsp(OnboardingData onboardingData) {
        InstitutionPspRequest institution = new InstitutionPspRequest();
        institution.institutionType(InstitutionType.valueOf(onboardingData.getInstitutionType().name()));
        institution.taxCode(onboardingData.getTaxCode());
        institution.subunitCode(onboardingData.getSubunitCode());
        institution.subunitType(Optional.ofNullable(onboardingData.getSubunitType())
                .map(InstitutionPaSubunitType::valueOf)
                .orElse(null));

        institution.setDescription(onboardingData.getInstitutionUpdate().getDescription());
        institution.digitalAddress(onboardingData.getInstitutionUpdate().getDigitalAddress());
        institution.address(onboardingData.getInstitutionUpdate().getAddress());
        institution.zipCode(onboardingData.getInstitutionUpdate().getZipCode());
        institution.geographicTaxonomyCodes(Optional.ofNullable(onboardingData.getInstitutionUpdate().getGeographicTaxonomies())
                .map(geotaxes -> geotaxes.stream()
                    .map(GeographicTaxonomy::getCode)
                    .collect(Collectors.toList()))
                .orElse(null));
        institution.rea(onboardingData.getInstitutionUpdate().getRea());
        institution.shareCapital(onboardingData.getInstitutionUpdate().getShareCapital());
        institution.businessRegisterPlace(onboardingData.getInstitutionUpdate().getBusinessRegisterPlace());
        institution.supportEmail(onboardingData.getInstitutionUpdate().getSupportEmail());
        institution.supportPhone(onboardingData.getInstitutionUpdate().getSupportPhone());
        institution.imported(onboardingData.getInstitutionUpdate().getImported());


        institution.setPaymentServiceProvider(toPaymentServiceProviderRequest(onboardingData.getInstitutionUpdate().getPaymentServiceProvider()));
        institution.setDataProtectionOfficer(toDataProtectionOfficerRequest(onboardingData.getInstitutionUpdate().getDataProtectionOfficer()));
        return institution;
    }

    PaymentServiceProviderRequest toPaymentServiceProviderRequest(PaymentServiceProvider paymentServiceProvider);
    DataProtectionOfficerRequest toDataProtectionOfficerRequest(DataProtectionOfficer dataProtectionOfficer);
}
