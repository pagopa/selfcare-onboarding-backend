package it.pagopa.selfcare.onboarding.web.handler;

import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.onboarding.connector.exceptions.ManagerNotFoundException;
import it.pagopa.selfcare.onboarding.connector.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.onboarding.core.exception.InvalidUserFieldsException;
import it.pagopa.selfcare.onboarding.core.exception.UpdateNotAllowedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice
public class OnboardingExceptionHandler {

    @ExceptionHandler({ResourceNotFoundException.class})
    @ResponseStatus(NOT_FOUND)
    Problem handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn(e.toString());
        return new Problem(NOT_FOUND, e.getMessage());
    }


    @ExceptionHandler({ManagerNotFoundException.class})
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    Problem handleProductHasNoRelationshipException(ManagerNotFoundException e) {
        log.warn(e.toString());
        return new Problem(INTERNAL_SERVER_ERROR, e.getMessage());
    }


    @ExceptionHandler({UpdateNotAllowedException.class})
    @ResponseStatus(CONFLICT)
    Problem handleUpdateNotAllowedException(UpdateNotAllowedException e) {
        log.warn(e.toString());
        return new Problem(CONFLICT, e.getMessage());
    }


    @ExceptionHandler({InvalidUserFieldsException.class})
    @ResponseStatus(CONFLICT)
    Problem handleInvalidUserFieldsException(InvalidUserFieldsException e) {
        log.warn(e.toString());
        final Problem problem = new Problem(CONFLICT, e.getMessage());
        problem.setInvalidParams(e.getInvalidFields().stream()
                .map(invalidField -> new Problem.InvalidParam(invalidField.getName(), invalidField.getReason()))
                .collect(Collectors.toList()));
        return problem;
    }

}
