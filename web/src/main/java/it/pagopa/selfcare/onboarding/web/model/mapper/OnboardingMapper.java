package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingResource;
import it.pagopa.selfcare.onboarding.web.model.OnboardingResponse;

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

}
