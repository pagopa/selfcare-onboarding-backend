package it.pagopa.selfcare.onboarding.web.handler;

import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.onboarding.connector.exceptions.ManagerNotFoundException;
import it.pagopa.selfcare.onboarding.connector.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.onboarding.core.exception.InvalidUserFieldsException;
import it.pagopa.selfcare.onboarding.core.exception.OnboardingNotAllowedException;
import it.pagopa.selfcare.onboarding.core.exception.UpdateNotAllowedException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;

class OnboardingExceptionHandlerTest {

    private static final String DETAIL_MESSAGE = "detail message";

    private final OnboardingExceptionHandler handler;

    public OnboardingExceptionHandlerTest() {
        this.handler = new OnboardingExceptionHandler();
    }

    @Test
    void handleResourceNotFoundException() {
        //given
        ResourceNotFoundException exceptionMock = mock(ResourceNotFoundException.class);
        when(exceptionMock.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        //when
        ResponseEntity<Problem> responseEntity = handler.handleResourceNotFoundException(exceptionMock);
        //then
        assertNotNull(responseEntity);
        assertEquals(NOT_FOUND, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(DETAIL_MESSAGE, responseEntity.getBody().getDetail());
        assertEquals(NOT_FOUND.value(), responseEntity.getBody().getStatus());
    }


    @Test
    void handleProductHasNoRelationshipException() {
        //given
        ManagerNotFoundException exceptionMock = mock(ManagerNotFoundException.class);
        when(exceptionMock.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        //when
        ResponseEntity<Problem> responseEntity = handler.handleProductHasNoRelationshipException(exceptionMock);
        //then
        assertNotNull(responseEntity);
        assertEquals(INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(DETAIL_MESSAGE, responseEntity.getBody().getDetail());
        assertEquals(INTERNAL_SERVER_ERROR.value(), responseEntity.getBody().getStatus());
    }


    @Test
    void handleUpdateNotAllowedException() {
        // given
        UpdateNotAllowedException mockException = mock(UpdateNotAllowedException.class);
        when(mockException.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        // when
        ResponseEntity<Problem> responseEntity = handler.handleUpdateNotAllowedException(mockException);
        // then
        assertNotNull(responseEntity);
        assertEquals(CONFLICT, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(DETAIL_MESSAGE, responseEntity.getBody().getDetail());
        assertEquals(CONFLICT.value(), responseEntity.getBody().getStatus());
    }


    @Test
    void handleInvalidUserFieldsException() {
        // given
        InvalidUserFieldsException mockException = mock(InvalidUserFieldsException.class);
        when(mockException.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        final InvalidUserFieldsException.InvalidField invalidField = new InvalidUserFieldsException.InvalidField("name", "reason");
        when(mockException.getInvalidFields())
                .thenReturn(List.of(invalidField));
        // when
        ResponseEntity<Problem> responseEntity = handler.handleInvalidUserFieldsException(mockException);
        // then
        assertNotNull(responseEntity);
        assertEquals(CONFLICT, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(DETAIL_MESSAGE, responseEntity.getBody().getDetail());
        assertEquals(CONFLICT.value(), responseEntity.getBody().getStatus());
        assertNotNull(responseEntity.getBody().getInvalidParams());
        assertEquals(1, responseEntity.getBody().getInvalidParams().size());
        assertEquals(invalidField.getName(), responseEntity.getBody().getInvalidParams().get(0).getName());
        assertEquals(invalidField.getReason(), responseEntity.getBody().getInvalidParams().get(0).getReason());
    }


    @Test
    void handleOnboardingNotAllowedException() {
        // given
        OnboardingNotAllowedException mockException = mock(OnboardingNotAllowedException.class);
        when(mockException.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        // when
        ResponseEntity<Problem> responseEntity = handler.handleOnboardingNotAllowedException(mockException);
        // then
        assertNotNull(responseEntity);
        assertEquals(FORBIDDEN, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(DETAIL_MESSAGE, responseEntity.getBody().getDetail());
        assertEquals(FORBIDDEN.value(), responseEntity.getBody().getStatus());
    }

}