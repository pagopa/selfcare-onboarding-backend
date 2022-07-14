package it.pagopa.selfcare.onboarding.web.handler;

import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.commons.web.model.mapper.ProblemMapper;
import it.pagopa.selfcare.onboarding.connector.exceptions.ManagerNotFoundException;
import it.pagopa.selfcare.onboarding.connector.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.onboarding.core.exception.InvalidUserFieldsException;
import it.pagopa.selfcare.onboarding.core.exception.OnboardingNotAllowedException;
import it.pagopa.selfcare.onboarding.core.exception.UpdateNotAllowedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice
public class OnboardingExceptionHandler {

    @ExceptionHandler({ResourceNotFoundException.class})
    ResponseEntity<Problem> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn(e.toString());
        return ProblemMapper.toResponseEntity(new Problem(NOT_FOUND, e.getMessage()));
    }


    @ExceptionHandler({ManagerNotFoundException.class})
    ResponseEntity<Problem> handleProductHasNoRelationshipException(ManagerNotFoundException e) {
        log.warn(e.toString());
        return ProblemMapper.toResponseEntity(new Problem(INTERNAL_SERVER_ERROR, e.getMessage()));
    }


    @ExceptionHandler({UpdateNotAllowedException.class})
    ResponseEntity<Problem> handleUpdateNotAllowedException(UpdateNotAllowedException e) {
        log.warn(e.toString());
        return ProblemMapper.toResponseEntity(new Problem(CONFLICT, e.getMessage()));
    }


    @ExceptionHandler({InvalidUserFieldsException.class})
    ResponseEntity<Problem> handleInvalidUserFieldsException(InvalidUserFieldsException e) {
        log.warn(e.toString());
        final Problem problem = new Problem(CONFLICT, e.getMessage());
        if (e.getInvalidFields() != null) {
            problem.setInvalidParams(e.getInvalidFields().stream()
                    .map(invalidField -> new Problem.InvalidParam(invalidField.getName(), invalidField.getReason()))
                    .collect(Collectors.toList()));
        }
        return ProblemMapper.toResponseEntity(problem);
    }


    @ExceptionHandler({OnboardingNotAllowedException.class})
    ResponseEntity<Problem> handleOnboardingNotAllowedException(OnboardingNotAllowedException e) {
        log.warn(e.toString());
        return ProblemMapper.toResponseEntity(new Problem(FORBIDDEN, e.getMessage()));
    }

}
