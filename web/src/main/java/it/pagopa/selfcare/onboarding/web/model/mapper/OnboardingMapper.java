package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.InstitutionOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.*;
import it.pagopa.selfcare.onboarding.web.model.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.stream.Collectors;

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
            resource.setGeographicTaxonomies(dto.getGeographicTaxonomies().stream()
                    .map(GeographicTaxonomyMapper::fromDto)
                    .collect(Collectors.toList()));
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
        }
        return resource;
    }


    public static InstitutionOnboardingInfoResource toResource(InstitutionOnboardingData model) {
        InstitutionOnboardingInfoResource resource = null;
        if (model != null) {
            resource = new InstitutionOnboardingInfoResource();
            resource.setManager(UserMapper.toResource(model.getManager()));
            resource.setInstitution(toData(model.getInstitution()));
            resource.setGeographicTaxonomies(model.getGeographicTaxonomies().stream()
                    .map(GeographicTaxonomyMapper::toResource)
                    .collect(Collectors.toList()));
        }
        return resource;
    }

    public static InstitutionData toData(InstitutionInfo model) {
        InstitutionData resource = null;
        if (model != null) {
            resource = new InstitutionData();
            BillingDataDto billing = new BillingDataDto();
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
        }
        return resource;
    }

}
