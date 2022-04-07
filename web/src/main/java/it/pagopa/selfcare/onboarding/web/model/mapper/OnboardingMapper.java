package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingResource;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
import it.pagopa.selfcare.onboarding.web.model.InstitutionResource;
import it.pagopa.selfcare.onboarding.web.model.OnboardingResponse;
import it.pagopa.selfcare.onboarding.web.model.UserDto;

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
