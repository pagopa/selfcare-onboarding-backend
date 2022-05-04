package it.pagopa.selfcare.onboarding.web.handler;

import it.pagopa.selfcare.commons.web.model.ErrorResource;
import it.pagopa.selfcare.onboarding.core.exceptions.InternalServerException;
import it.pagopa.selfcare.onboarding.core.exceptions.ProductHasNoRelationshipException;
import it.pagopa.selfcare.onboarding.core.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class OnboardingExceptionHandler {

    @ExceptionHandler({ResourceNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResource handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn(e.getMessage());
        return new ErrorResource(e.getMessage());
    }

    @ExceptionHandler({ProductHasNoRelationshipException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResource handleProductHasNoRelationshipException(ProductHasNoRelationshipException e) {
        log.warn(e.getMessage());
        return new ErrorResource(e.getMessage());
    }

    @ExceptionHandler({InternalServerException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ErrorResource handleInternalServerException(InternalServerException e) {
        log.warn(e.getMessage());
        return new ErrorResource(e.getMessage());
    }
}
