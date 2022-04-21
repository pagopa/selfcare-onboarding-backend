package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingResource;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

class OnboardingMapperTest {


    @Getter
    @Setter
    private static class DummyOnboardingResource implements OnboardingResource {
        private String token;
        private File document;
    }

}