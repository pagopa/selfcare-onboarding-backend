package it.pagopa.selfcare.onboarding.web.handler;

import it.pagopa.selfcare.commons.web.model.ErrorResource;
import it.pagopa.selfcare.onboarding.core.exceptions.InternalServerException;
import it.pagopa.selfcare.onboarding.core.exceptions.ProductHasNoRelationshipException;
import it.pagopa.selfcare.onboarding.core.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        ErrorResource resource = handler.handleResourceNotFoundException(exceptionMock);
        //then
        assertNotNull(resource);
        assertEquals(DETAIL_MESSAGE, resource.getMessage());
    }

    @Test
    void handleInternalServerException() {
        //given
        InternalServerException exceptionMock = Mockito.mock(InternalServerException.class);
        Mockito.when(exceptionMock.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        //when
        ErrorResource resource = handler.handleInternalServerException(exceptionMock);
        //then
        assertNotNull(resource);
        assertEquals(DETAIL_MESSAGE, resource.getMessage());
    }

    @Test
    void handleProductHasNoRelationshipException() {
        //given
        ProductHasNoRelationshipException exceptionMock = Mockito.mock(ProductHasNoRelationshipException.class);
        Mockito.when(exceptionMock.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        //when
        ErrorResource resource = handler.handleProductHasNoRelationshipException(exceptionMock);
        //then
        assertNotNull(resource);
        assertEquals(DETAIL_MESSAGE, resource.getMessage());
    }
}