package it.pagopa.selfcare.onboarding.connector.rest.mapper;


import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.OnboardingDefaultRequest;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.OnboardingPaRequest;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.OnboardingPspRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OnboardingMapper {

    OnboardingPaRequest toOnboardingPaRequest(OnboardingData onboardingData);
    OnboardingPspRequest toOnboardingPspRequest(OnboardingData onboardingData);
    OnboardingDefaultRequest toOnboardingDefaultRequest(OnboardingData onboardingData);
}
