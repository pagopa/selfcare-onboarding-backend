package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.BillingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingResource;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
import it.pagopa.selfcare.onboarding.web.model.*;

public class OnboardingMapper {

    public static OnboardingResponse toResource(OnboardingResource model) {
        OnboardingResponse resource = null;

        if (model != null) {
            resource = new OnboardingResponse();
            resource.setToken(model.getToken());
            resource.setDocument(model.getDocument());
        }

        return resource;
    }

    public static BillingData fromDto(BillingDataDto model) {
        BillingData resource = null;
        if (model != null) {
            resource = new BillingData();
            resource.setDescription(model.getBusinessName());
            resource.setTaxCode(model.getTaxCode());
            resource.setDigitalAddress(model.getDigitalAddress());
            resource.setVatNumber(model.getVatNumber());
            resource.setPhysicalAddress(model.getRegisteredOffice());
            if (model.getPublicService() != null) {
                resource.setPublicService(true);
            }
        }
        return resource;
    }

    public static OnboardingData toOnboardingData(String institutionId, String productId, OnboardingDto model) {
        OnboardingData resource = null;
        if (model != null) {
            resource = new OnboardingData();
            resource.setUsers(model.getUsers());
            resource.setInstitutionId(institutionId);
            resource.setProductId(productId);
            if (model.getBillingData() != null) {
                resource.setBillingData(fromDto(model.getBillingData()));
                resource.setOrganizationType(model.getOrganizationType());
            }
        }
        return resource;
    }

    public static InstitutionResource toResource(InstitutionInfo model) {
        InstitutionResource resource = null;
        if (model != null) {
            resource = new InstitutionResource();
            resource.setDescription(model.getDescription());
            resource.setInstitutionId(model.getInstitutionId());
            resource.setAddress(model.getAddress());
            resource.setDigitalAddress(model.getDigitalAddress());
            resource.setTaxCode(model.getTaxCode());
            resource.setZipCode(model.getZipCode());
        }
        return resource;
    }


    public static UserDto toUserDto(UserInfo model) {
        UserDto dto = null;
        return dto;
    }

}
