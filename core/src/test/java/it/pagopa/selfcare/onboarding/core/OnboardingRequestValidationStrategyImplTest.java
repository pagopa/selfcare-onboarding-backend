package it.pagopa.selfcare.onboarding.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import javax.validation.ValidationException;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OnboardingRequestValidationStrategyImplTest {

    @Test
    void validate_validationNotConfigured() {
        // given
        final Map<String, Set<String>> institutionProductsDisallowedList = null;
        final Map<String, Set<String>> institutionProductsAllowedList = null;
        final String productId = "prod-io";
        final String institutionExternalId = "inst-1";
        final OnboardingRequestValidationStrategyImpl validationStrategy =
                new OnboardingRequestValidationStrategyImpl(institutionProductsDisallowedList, institutionProductsAllowedList);
        // when
        final Executable executable = () -> validationStrategy.validate(productId, institutionExternalId);
        // then
        assertDoesNotThrow(executable);
    }


    @Test
    void validate_productNotInConfig() {
        // given
        final Map<String, Set<String>> institutionProductsDisallowedList = Map.of();
        final Map<String, Set<String>> institutionProductsAllowedList = Map.of();
        final String productId = "prod-io";
        final String institutionExternalId = "inst-1";
        final OnboardingRequestValidationStrategyImpl validationStrategy =
                new OnboardingRequestValidationStrategyImpl(institutionProductsDisallowedList, institutionProductsAllowedList);
        // when
        final Executable executable = () -> validationStrategy.validate(productId, institutionExternalId);
        // then
        assertDoesNotThrow(executable);
    }


    @Test
    void validate_invalidConfig_invalidUsageOfSpecialCharacterInDisallowedList() {
        // given
        final Map<String, Set<String>> institutionProductsDisallowedList = Map.of("prod-io", Set.of("inst-1", "*"));
        final Map<String, Set<String>> institutionProductsAllowedList = null;
        // when
        final Executable executable = () -> new OnboardingRequestValidationStrategyImpl(institutionProductsDisallowedList, institutionProductsAllowedList);
        // then
        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("Invalid configuration: bad using of special character '*' in disallowed-list for key 'prod-io'. If used, the '*' is the only value allowed for a given key", e.getMessage());
    }


    @Test
    void validate_invalidConfig_invalidUsageOfSpecialCharacterInAllowedList() {
        // given
        final Map<String, Set<String>> institutionProductsDisallowedList = null;
        final Map<String, Set<String>> institutionProductsAllowedList = Map.of("prod-io", Set.of("inst-1", "*"));
        // when
        final Executable executable = () -> new OnboardingRequestValidationStrategyImpl(institutionProductsDisallowedList, institutionProductsAllowedList);
        // then
        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("Invalid configuration: bad using of special character '*' in allowed-list for key 'prod-io'. If used, the '*' is the only value allowed for a given key", e.getMessage());
    }


    @Test
    void validate_invalidConfig_sameValeForSameKey() {
        // given
        final Map<String, Set<String>> institutionProductsDisallowedList = Map.of("prod-io", Set.of("inst-1"));
        final Map<String, Set<String>> institutionProductsAllowedList = Map.of("prod-io", Set.of("inst-1"));
        // when
        final Executable executable = () -> new OnboardingRequestValidationStrategyImpl(institutionProductsDisallowedList, institutionProductsAllowedList);
        // then
        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("Invalid configuration: 'prod-io' key have a 'inst-1' value for both disallowed list and allowed list", e.getMessage());
    }


    @Test
    void validate_institutionExplicitlyInDisallowedList() {
        // given
        final Map<String, Set<String>> institutionProductsDisallowedList =
                Map.of("prod-io", Set.of("inst-1", "inst-2"),
                        "prod-interop", Set.of("inst-3"),
                        "prod-cg", Set.of("*"));
        final Map<String, Set<String>> institutionProductsAllowedList =
                Map.of("prod-pn", Set.of("*"),
                        "prod-io", Set.of("inst-3"));
        final String productId = "prod-io";
        final String institutionExternalId = "inst-1";
        final OnboardingRequestValidationStrategyImpl validationStrategy =
                new OnboardingRequestValidationStrategyImpl(institutionProductsDisallowedList, institutionProductsAllowedList);
        // when
        final Executable executable = () -> validationStrategy.validate(productId, institutionExternalId);
        // then
        final ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals(String.format("Institution with external id '%s' is not allowed to onboard '%s' product",
                institutionExternalId,
                productId),
                e.getMessage());
    }


    @Test
    void validate_institutionImplicitlyInDisallowedList() {
        // given
        final Map<String, Set<String>> institutionProductsDisallowedList =
                Map.of("prod-io", Set.of("inst-1", "inst-2"),
                        "prod-interop", Set.of("inst-3"),
                        "prod-cg", Set.of("*"));
        final Map<String, Set<String>> institutionProductsAllowedList =
                Map.of("prod-pn", Set.of("*"),
                        "prod-io", Set.of("inst-3"));
        final String productId = "prod-io";
        final String institutionExternalId = "inst-1";
        final OnboardingRequestValidationStrategyImpl validationStrategy =
                new OnboardingRequestValidationStrategyImpl(institutionProductsDisallowedList, institutionProductsAllowedList);
        // when
        final Executable executable = () -> validationStrategy.validate(productId, institutionExternalId);
        // then
        final ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals(String.format("Institution with external id '%s' is not allowed to onboard '%s' product",
                institutionExternalId,
                productId),
                e.getMessage());
    }

    //    @Test
    void validate_institutionExplicitlyInDisallowedList2() {
        // given
        final Map<String, Set<String>> institutionProductsDisallowedList =
                Map.of("prod-io", Set.of("inst-1", "inst-2"),
                        "prod-interop", Set.of("inst-3"),
                        "prod-cg", Set.of("*"));
        final Map<String, Set<String>> institutionProductsAllowedList =
                Map.of("prod-pn", Set.of("*"),
                        "prod-io", Set.of("inst-3"));
        final String productId = null;
        final String institutionExternalId = null;
        final OnboardingRequestValidationStrategyImpl validationStrategy =
                new OnboardingRequestValidationStrategyImpl(institutionProductsDisallowedList, institutionProductsAllowedList);
        // when
        final Executable executable = () -> validationStrategy.validate(productId, institutionExternalId);
        // then
        assertDoesNotThrow(executable);
    }

}