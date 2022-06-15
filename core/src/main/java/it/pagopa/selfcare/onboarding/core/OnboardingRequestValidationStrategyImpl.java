package it.pagopa.selfcare.onboarding.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class OnboardingRequestValidationStrategyImpl implements OnboardingRequestValidationStrategy {

    /**
     * It represent, if present, the institutions and products not allowed to be onboarded (i.e. a disallowed-list).
     * The {@code Map} has as key the product id  and as values a list of institution external id not allowed for that product.
     * A {@code *} value means "anything".
     */
    private final Optional<Map<String, Set<String>>> institutionProductsDisallowedList;
    /**
     * It represent, if present, the institutions and products allowed to be onboarded (i.e. an allowed-list).
     * The {@code Map} has as key the product id  and as values a list of institution external id allowed for that product
     * A {@code *} value means "anything".
     */
    private final Optional<Map<String, Set<String>>> institutionProductsAllowedList;


    @Autowired
    public OnboardingRequestValidationStrategyImpl(@Value("#{${onboarding.institutions-disallowed-list}}") Map<String, Set<String>> institutionProductsDisallowedList,
                                                   @Value("#{${onboarding.institutions-allowed-list}}") Map<String, Set<String>> institutionProductsAllowedList) {
        this.institutionProductsDisallowedList = Optional.ofNullable(institutionProductsDisallowedList);
        this.institutionProductsAllowedList = Optional.ofNullable(institutionProductsAllowedList);
//        this.institutionProductsDisallowedList.ifPresent(disallowedList -> disallowedList.forEach((productId, institutionExternalIds) -> {
//            if (institutionExternalIds.size() > 1
//                    && institutionExternalIds.stream().anyMatch("*"::equals)) {
//                throw new IllegalArgumentException(String.format("Invalid configuration: bad using of special character '*' in disallowed-list for key '%s'. If used, the '*' is the only value allowed for a given key",
//                        productId));
//            }
//        }));
        validateSpecialcharecterUsage(this.institutionProductsDisallowedList, "disallowed-list");
        validateSpecialcharecterUsage(this.institutionProductsAllowedList, "allowed-list");
        this.institutionProductsDisallowedList.ifPresent(disallowedList -> disallowedList.forEach((productId, institutionExternalIds) ->
                this.institutionProductsAllowedList.ifPresent(allowedList -> {
                    if (allowedList.containsKey(productId)) {
                        institutionExternalIds.stream()
                                .filter(institutionExternalId -> allowedList.get(productId).contains(institutionExternalId))
                                .findAny().ifPresent(institutionExternalId -> {
                            throw new IllegalArgumentException(String.format("Invalid configuration: '%s' key have a '%s' value for both disallowed list and allowed list",
                                    productId,
                                    institutionExternalId));
                        });
                    }
                })));
    }

    private void validateSpecialcharecterUsage(Optional<Map<String, Set<String>>> institutionProductsMap, String listName) {
        institutionProductsMap.ifPresent(disallowedList -> disallowedList.forEach((productId, institutionExternalIds) -> {
            if (institutionExternalIds.size() > 1
                    && institutionExternalIds.stream().anyMatch("*"::equals)) {
                throw new IllegalArgumentException(String.format("Invalid configuration: bad using of special character '*' in %s for key '%s'. If used, the '*' is the only value allowed for a given key",
                        listName,
                        productId));
            }
        }));
    }


    @Override
    public void validate(String productId, String institutionExternalId) {
        boolean allowed = true;
        if (institutionProductsDisallowedList.isPresent()) {
            if (institutionProductsDisallowedList.get().containsKey(productId)) {
                if (institutionProductsDisallowedList.get().get(productId).contains(institutionExternalId)) {
                    throw new ValidationException(String.format("Institution with external id '%s' is not allowed to onboard '%s' product",
                            institutionExternalId,
                            productId));
                } else {
                    allowed = !institutionProductsDisallowedList.get().get(productId).contains("*");
                }
            }
        }
        if (institutionProductsAllowedList.isPresent()) {
            if (institutionProductsAllowedList.get().containsKey(productId)) {
                if (institutionProductsAllowedList.get().get(productId).contains(institutionExternalId)) {
                    allowed = true;
                } else {
                    allowed = institutionProductsAllowedList.get().get(productId).contains("*");
                }
            }
        }
        if (!allowed) {
            throw new ValidationException(String.format("Institution with external id '%s' is not allowed to onboard '%s' product",
                    institutionExternalId,
                    productId));
        }
    }


    public static void main(String[] args) {

        final Map<String, Set<String>> institutionProductsDisallowedList = Map.of("prod-io", Set.of("inst-1", "inst-2"), "prod-interop", Set.of("inst-3"), "prod-cg", Set.of("*"));
        final Map<String, Set<String>> institutionProductsAllowedList = Map.of("prod-pn", Set.of("*"), "prod-io", Set.of("inst-3"));
        final OnboardingRequestValidationStrategyImpl onboardingRequestValidationStrategy = new OnboardingRequestValidationStrategyImpl(
                institutionProductsDisallowedList,
                institutionProductsAllowedList);

        // prodotto non gestito esplicitamente nelle config-list
//        onboardingRequestValidationStrategy.validate("not-a-prod", "inst-3");

        // prodotto presente esplicitamente in disallowed-list
        onboardingRequestValidationStrategy.validate("prod-io", "inst-1");

        // prodotto presente esplicitamente in disallowed-list
        onboardingRequestValidationStrategy.validate("prod-io", "inst-1");
    }

}
