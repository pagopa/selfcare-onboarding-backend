package it.pagopa.selfcare.onboarding.web.model;

import it.pagopa.selfcare.commons.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CompanyBillingDataDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    void validateNullFields() {
        HashMap<String, Class<? extends Annotation>> toCheckMap = new HashMap<>();
        toCheckMap.put("taxCode", NotBlank.class);
        toCheckMap.put("businessName", NotBlank.class);
        toCheckMap.put("certified", NotNull.class);
        toCheckMap.put("digitalAddress", NotBlank.class);

        CompanyBillingDataDto billingDataDto = new CompanyBillingDataDto();
        //when
        Set<ConstraintViolation<Object>> violations = validator.validate(billingDataDto);
        // then
        List<ConstraintViolation<Object>> filteredViolations = violations.stream()
                .filter(violation -> {
                    Class<? extends Annotation> annotationToCheck = toCheckMap.get(violation.getPropertyPath().toString());
                    return !violation.getConstraintDescriptor().getAnnotation().annotationType().equals(annotationToCheck);
                })
                .collect(Collectors.toList());
        assertTrue(filteredViolations.isEmpty());
    }

    @Test
    void validateNotNullFields() {
        // given
        CompanyBillingDataDto billingDataDto = TestUtils.mockInstance(new CompanyBillingDataDto());
        // when
        Set<ConstraintViolation<Object>> violations = validator.validate(billingDataDto);
        // then
        assertTrue(violations.isEmpty());
    }
}