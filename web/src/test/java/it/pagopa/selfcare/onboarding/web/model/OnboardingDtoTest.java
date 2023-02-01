package it.pagopa.selfcare.onboarding.web.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnboardingDtoTest {

    private Validator validator;


    @BeforeEach
    void setUp() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }


    @Test
    void validateNullFields() {
        // given
        HashMap<String, Class<? extends Annotation>> toCheckMap = new HashMap<>();
        toCheckMap.put("users", NotEmpty.class);
        toCheckMap.put("billingData", NotNull.class);
        toCheckMap.put("institutionType", NotNull.class);
        toCheckMap.put("geographicTaxonomies", NotNull.class);
        OnboardingDto model = new OnboardingDto();
        // when
        Set<ConstraintViolation<Object>> violations = validator.validate(model);
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
        OnboardingDto model = mockInstance(new OnboardingDto());
        UserDto userDto = mockInstance(new UserDto());
        userDto.setEmail("email@example.com");
        GeographicTaxonomyDto geographicTaxonomyDto = mockInstance(new GeographicTaxonomyDto());
        model.setUsers(List.of(userDto));
        model.setGeographicTaxonomies(List.of(geographicTaxonomyDto));
        model.setBillingData(mockInstance(new BillingDataDto()));
        PspDataDto pspDataDto = mockInstance(new PspDataDto());
        DpoDataDto dpoDataDto = mockInstance(new DpoDataDto());
        dpoDataDto.setEmail("email@example.com");
        dpoDataDto.setPec("email@example.com");
        pspDataDto.setDpoData(dpoDataDto);
        model.setPspData(pspDataDto);
        model.getAssistanceContacts().setSupportEmail("email@example.com");
        // when
        Set<ConstraintViolation<Object>> violations = validator.validate(model);
        // then
        assertTrue(violations.isEmpty());
    }
}