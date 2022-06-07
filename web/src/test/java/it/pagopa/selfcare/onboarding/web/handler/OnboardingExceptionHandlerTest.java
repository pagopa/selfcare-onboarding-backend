package it.pagopa.selfcare.onboarding.web.handler;

import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.onboarding.connector.exceptions.ManagerNotFoundException;
import it.pagopa.selfcare.onboarding.connector.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.onboarding.core.exception.UpdateNotAllowedException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        ResourceNotFoundException exceptionMock = Mockito.mock(ResourceNotFoundException.class);
        Mockito.when(exceptionMock.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        //when
        Problem resource = handler.handleResourceNotFoundException(exceptionMock);
        //then
        assertNotNull(resource);
        assertEquals(DETAIL_MESSAGE, resource.getDetail());
        assertEquals(NOT_FOUND.value(), resource.getStatus());
    }


    @Test
    void handleProductHasNoRelationshipException() {
        //given
        ManagerNotFoundException exceptionMock = Mockito.mock(ManagerNotFoundException.class);
        Mockito.when(exceptionMock.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        //when
        Problem resource = handler.handleProductHasNoRelationshipException(exceptionMock);
        //then
        assertNotNull(resource);
        assertEquals(DETAIL_MESSAGE, resource.getDetail());
        assertEquals(INTERNAL_SERVER_ERROR.value(), resource.getStatus());
    }


    @Test
    void handleUpdateNotAllowedException() {
        // given
        UpdateNotAllowedException mockException = Mockito.mock(UpdateNotAllowedException.class);
        Mockito.when(mockException.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        // when
        Problem resource = handler.handleUpdateNotAllowedException(mockException);
        // then
        assertNotNull(resource);
        assertEquals(DETAIL_MESSAGE, resource.getDetail());
        assertEquals(CONFLICT.value(), resource.getStatus());
    }

}