package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.InstitutionOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.Billing;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionUpdate;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.web.model.BillingDataDto;
import it.pagopa.selfcare.onboarding.web.model.InstitutionData;
import it.pagopa.selfcare.onboarding.web.model.InstitutionOnboardingInfoResource;
import it.pagopa.selfcare.onboarding.web.model.OnboardingDto;
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

    public static InstitutionUpdate mapInstitutionUpdate(BillingDataDto dto) {
        InstitutionUpdate resource = null;
        if (dto != null) {
            resource = new InstitutionUpdate();
            resource.setAddress(dto.getRegisteredOffice());
            resource.setDigitalAddress(dto.getDigitalAddress());
            resource.setDescription(dto.getBusinessName());
            resource.setTaxCode(dto.getTaxCode());
        }
        return resource;
    }

    public static OnboardingData toOnboardingData(String institutionId, String productId, OnboardingDto model) {
        OnboardingData resource = null;
        if (model != null) {
            resource = new OnboardingData();
            resource.setUsers(model.getUsers().stream()
                    .map(UserMapper::toUser)
                    .collect(Collectors.toList()));
            resource.setInstitutionId(institutionId);
            resource.setProductId(productId);
            resource.setOrigin(model.getOrigin());
            resource.setPricingPlan(model.getPricingPlan());
            resource.setInstitutionUpdate(mapInstitutionUpdate(model.getBillingData()));
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
