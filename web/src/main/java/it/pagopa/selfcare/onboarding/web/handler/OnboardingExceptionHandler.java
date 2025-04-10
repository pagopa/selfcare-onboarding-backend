package it.pagopa.selfcare.onboarding.web.handler;

import feign.FeignException;
import it.pagopa.selfcare.commons.web.handler.FeignExceptionsHandler;
import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.commons.web.model.mapper.ProblemMapper;
import it.pagopa.selfcare.onboarding.connector.exceptions.InternalGatewayErrorException;
import it.pagopa.selfcare.onboarding.connector.exceptions.InvalidRequestException;
import it.pagopa.selfcare.onboarding.connector.exceptions.ManagerNotFoundException;
import it.pagopa.selfcare.onboarding.connector.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.onboarding.core.exception.InvalidUserFieldsException;
import it.pagopa.selfcare.onboarding.core.exception.OnboardingNotAllowedException;
import it.pagopa.selfcare.onboarding.core.exception.UpdateNotAllowedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice
public class OnboardingExceptionHandler extends FeignExceptionsHandler {

    @ExceptionHandler({InvalidRequestException.class})
    ResponseEntity<Problem> handleInvalidRequestException(InvalidRequestException e) {
        log.warn(e.toString());
        return ProblemMapper.toResponseEntity(new Problem(BAD_REQUEST, e.getMessage()));
    }

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

    @ExceptionHandler({InternalGatewayErrorException.class})
    ResponseEntity<Problem> handleInternalGatewayErrorException(InternalGatewayErrorException e) {
        log.warn(e.toString());
        return ProblemMapper.toResponseEntity(new Problem(BAD_GATEWAY, e.getMessage()));
    }

    @Override
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Problem> handleFeignException(FeignException e) {
        log.info("Onboarding handler called!");
        log.error("Feign error: {}", e.contentUTF8(), e);

        HttpStatus httpStatus = Optional.ofNullable(HttpStatus.resolve(e.status()))
                .filter(status -> !status.is2xxSuccessful())
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);

        return ProblemMapper.toResponseEntity(new Problem(httpStatus, e.contentUTF8()));
    }
}
