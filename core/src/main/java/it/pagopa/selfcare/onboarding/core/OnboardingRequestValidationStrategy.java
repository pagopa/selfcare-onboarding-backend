package it.pagopa.selfcare.onboarding.core;

public interface OnboardingRequestValidationStrategy {

    void validate(String productId, String institutionExternalId);

}
