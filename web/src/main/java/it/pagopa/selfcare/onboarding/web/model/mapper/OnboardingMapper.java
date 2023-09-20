package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.InstitutionLegalAddressData;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.AssistanceContacts;
import it.pagopa.selfcare.onboarding.connector.model.institutions.CompanyInformations;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.*;
import it.pagopa.selfcare.onboarding.web.model.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @deprecated [reference SELC-2815]
 * It's better using sdk such as https://reflectoring.io/java-mapping-with-mapstruct/, look at @classes OnboardingResourceMapper
 * */
@Deprecated(forRemoval = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OnboardingMapper {

    public static Billing fromDto(BillingDataDto model) {
        Billing resource = null;
        if (model != null) {
            resource = new Billing();
            resource.setVatNumber(model.getVatNumber());
            resource.setRecipientCode(model.getRecipientCode());
            if (model.getPublicServices() != null) {
                resource.setPublicServices(model.getPublicServices());
            }
        }
        return resource;
    }

    private static InstitutionUpdate mapInstitutionUpdate(OnboardingDto dto) {
        InstitutionUpdate resource = null;
        if (dto != null && dto.getBillingData() != null) {
            resource = new InstitutionUpdate();
            resource.setAddress(dto.getBillingData().getRegisteredOffice());
            resource.setDigitalAddress(dto.getBillingData().getDigitalAddress());
            resource.setZipCode(dto.getBillingData().getZipCode());
            resource.setDescription(dto.getBillingData().getBusinessName());
            resource.setTaxCode(dto.getBillingData().getTaxCode());

            resource.setPaymentServiceProvider(mapPaymentServiceProvider(dto.getPspData()));
            resource.setDataProtectionOfficer(mapDataProtectionOfficer(dto.getPspData()));

            if(Objects.nonNull(dto.getGeographicTaxonomies())) {
                resource.setGeographicTaxonomies(dto.getGeographicTaxonomies().stream()
                        .map(GeographicTaxonomyMapper::fromDto)
                        .collect(Collectors.toList()));
            }

            if (dto.getCompanyInformations() != null) {
                resource.setRea(dto.getCompanyInformations().getRea());
                resource.setShareCapital(dto.getCompanyInformations().getShareCapital());
                resource.setBusinessRegisterPlace(dto.getCompanyInformations().getBusinessRegisterPlace());
            }
            if (dto.getAssistanceContacts() != null) {
                resource.setSupportEmail(dto.getAssistanceContacts().getSupportEmail());
                resource.setSupportPhone(dto.getAssistanceContacts().getSupportPhone());
            }
            resource.setImported(false);
        }
        return resource;
    }


    private static PaymentServiceProvider mapPaymentServiceProvider(PspDataDto dto) {
        PaymentServiceProvider resource = null;
        if (dto != null) {
            resource = new PaymentServiceProvider();
            resource.setAbiCode(dto.getAbiCode());
            resource.setBusinessRegisterNumber(dto.getBusinessRegisterNumber());
            resource.setLegalRegisterName(dto.getLegalRegisterName());
            resource.setLegalRegisterNumber(dto.getLegalRegisterNumber());
            resource.setVatNumberGroup(dto.getVatNumberGroup());
        }
        return resource;
    }


    private static DataProtectionOfficer mapDataProtectionOfficer(PspDataDto dto) {
        DataProtectionOfficer resource = null;
        if (dto != null && dto.getDpoData() != null) {
            resource = new DataProtectionOfficer();
            resource.setAddress(dto.getDpoData().getAddress());
            resource.setEmail(dto.getDpoData().getEmail());
            resource.setPec(dto.getDpoData().getPec());
        }
        return resource;
    }


    public static OnboardingData toOnboardingData(String externalId, String productId, OnboardingDto model) {
        OnboardingData resource = null;
        if (model != null) {
            resource = new OnboardingData();
            resource.setUsers(model.getUsers().stream()
                    .map(UserMapper::toUser)
                    .collect(Collectors.toList()));
            resource.setInstitutionExternalId(externalId);
            resource.setProductId(productId);
            resource.setOrigin(model.getOrigin());
            resource.setPricingPlan(model.getPricingPlan());
            resource.setInstitutionUpdate(mapInstitutionUpdate(model));
            if (model.getBillingData() != null) {
                resource.setBilling(fromDto(model.getBillingData()));
            }
            resource.setInstitutionType(model.getInstitutionType());
            if(InstitutionType.PG.equals(model.getInstitutionType()) && productId.startsWith("prod-pn-pg")) {
                resource.setBusinessName(model.getBillingData().getBusinessName());
                resource.setExistsInRegistry(model.getBillingData().isCertified());
            }
        }
        return resource;
    }


    public static InstitutionOnboardingInfoResource toResource(InstitutionOnboardingData model) {
        InstitutionOnboardingInfoResource resource = null;
        if (model != null) {
            resource = new InstitutionOnboardingInfoResource();
            resource.setInstitution(toData(model.getInstitution()));
            resource.setGeographicTaxonomies(Optional.ofNullable(model.getGeographicTaxonomies())
                            .map(geotaxes -> geotaxes.stream()
                                .map(GeographicTaxonomyMapper::toResource)
                                .collect(Collectors.toList()))
                            .orElse(null)
            );
            resource.getInstitution().setCompanyInformations(toResource(model.getCompanyInformations()));
            resource.getInstitution().setAssistanceContacts(toResource(model.getAssistanceContacts()));
        }
        return resource;
    }

    public static AssistanceContactsResource toResource(AssistanceContacts model) {
        AssistanceContactsResource resource = null;
        if (model != null) {
            resource = new AssistanceContactsResource();
            resource.setSupportEmail(model.getSupportEmail());
            resource.setSupportPhone(model.getSupportPhone());
        }
        return resource;
    }

    public static CompanyInformationsResource toResource(CompanyInformations model) {
        CompanyInformationsResource resource = null;
        if (model != null) {
            resource = new CompanyInformationsResource();
            resource.setRea(model.getRea());
            resource.setShareCapital(model.getShareCapital());
            resource.setBusinessRegisterPlace(model.getBusinessRegisterPlace());
        }
        return resource;
    }

    public static InstitutionData toData(InstitutionInfo model) {
        InstitutionData resource = null;
        if (model != null) {
            resource = new InstitutionData();
            BillingDataResponseDto billing = new BillingDataResponseDto();
            billing.setDigitalAddress(model.getDigitalAddress());
            billing.setTaxCode(model.getTaxCode());
            billing.setBusinessName(model.getDescription());
            billing.setRegisteredOffice(model.getAddress());
            billing.setZipCode(model.getZipCode());
            if (model.getBilling() != null) {
                billing.setPublicServices(model.getBilling().getPublicServices());
                billing.setRecipientCode(model.getBilling().getRecipientCode());
                billing.setVatNumber(model.getBilling().getVatNumber());
            }
            resource.setBillingData(billing);
            resource.setOrigin(model.getOrigin());
            resource.setInstitutionType(model.getInstitutionType());
            resource.setId(model.getId());
        }
        return resource;
    }

    public static InstitutionLegalAddressResource toResource(InstitutionLegalAddressData model) {
        InstitutionLegalAddressResource resource = null;
        if (model != null) {
            resource = new InstitutionLegalAddressResource();

            resource.setAddress(model.getAddress());
            resource.setZipCode(model.getZipCode());
        }
        return resource;
    }

}
