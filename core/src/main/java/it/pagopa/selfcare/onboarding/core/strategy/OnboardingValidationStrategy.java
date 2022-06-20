package it.pagopa.selfcare.onboarding.core.strategy;

public interface OnboardingValidationStrategy {

    boolean validate(String productId, String institutionExternalId);

}
