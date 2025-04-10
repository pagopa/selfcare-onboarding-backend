package it.pagopa.selfcare.onboarding.web.handler;

import feign.FeignException;
import feign.Request;
import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.onboarding.connector.exceptions.InternalGatewayErrorException;
import it.pagopa.selfcare.onboarding.connector.exceptions.InvalidRequestException;
import it.pagopa.selfcare.onboarding.connector.exceptions.ManagerNotFoundException;
import it.pagopa.selfcare.onboarding.connector.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.onboarding.core.exception.InvalidUserFieldsException;
import it.pagopa.selfcare.onboarding.core.exception.OnboardingNotAllowedException;
import it.pagopa.selfcare.onboarding.core.exception.UpdateNotAllowedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    void handleInvalidRequestException() {
        //given
        InvalidRequestException exceptionMock = mock(InvalidRequestException.class);
        when(exceptionMock.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        //when
        ResponseEntity<Problem> responseEntity = handler.handleInvalidRequestException(exceptionMock);
        //then
        assertNotNull(responseEntity);
        assertEquals(BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(DETAIL_MESSAGE, responseEntity.getBody().getDetail());
        assertEquals(BAD_REQUEST.value(), responseEntity.getBody().getStatus());
    }

    @Test
    void handleInternalGatewayErrorException() {
        //given
        InternalGatewayErrorException exceptionMock = mock(InternalGatewayErrorException.class);
        when(exceptionMock.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        //when
        ResponseEntity<Problem> responseEntity = handler.handleInternalGatewayErrorException(exceptionMock);
        //then
        assertNotNull(responseEntity);
        assertEquals(BAD_GATEWAY, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(DETAIL_MESSAGE, responseEntity.getBody().getDetail());
        assertEquals(BAD_GATEWAY.value(), responseEntity.getBody().getStatus());
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

    @AfterEach
    void cleanup() {
        RequestContextHolder.resetRequestAttributes(); // Pulisce dopo il test
    }

    @Test
    void shouldReturnCustomProblemWithInstancePath() {
        // given
        String feignBody = "{\"status\":400,\"errors\":[{\"code\":\"002-1003\",\"detail\":\"Only CAdES signature form is admitted. Invalid signatures forms detected: PAdES\"}]}";

        FeignException feignException = new FeignException.BadRequest(
                "Bad Request",
                Request.create(
                        Request.HttpMethod.POST,
                        "/v2/tokens/565ad8a7-db6d-4f03-9f00-d835c63a0d1a/complete",
                        Collections.emptyMap(),
                        null,
                        StandardCharsets.UTF_8,
                        null
                ),
                feignBody.getBytes(StandardCharsets.UTF_8),
                Map.of("Content-Type", Collections.singletonList("application/json"))
        );

        // Simula una HTTP request attiva
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", "/v2/tokens/565ad8a7-db6d-4f03-9f00-d835c63a0d1a/complete");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

        // when
        ResponseEntity<Problem> response = handler.handleFeignException(feignException);

        // then
        assertNotNull(response);
        Problem problem = response.getBody();
        assertNotNull(problem);
        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), problem.getTitle());
        assertEquals(feignBody, problem.getDetail());
        assertEquals("/v2/tokens/565ad8a7-db6d-4f03-9f00-d835c63a0d1a/complete", problem.getInstance());
    }

    @Test
    void shouldReturn500WhenFeignExceptionIsInternalServerError() {
        // given
        String feignBody = "{\"status\":500,\"errors\":[{\"code\":\"999-9999\",\"detail\":\"Generic downstream failure\"}]}";

        FeignException feignException = new FeignException.InternalServerError(
                "Internal Server Error",
                Request.create(
                        Request.HttpMethod.POST,
                        "/v1/users/onboarding",
                        Collections.emptyMap(),
                        null,
                        StandardCharsets.UTF_8,
                        null
                ),
                feignBody.getBytes(StandardCharsets.UTF_8),
                Map.of("Content-Type", Collections.singletonList("application/json"))
        );

        // Simula richiesta HTTP per valorizzare `instance`
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", "/v1/users/onboarding");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

        // when
        ResponseEntity<Problem> response = handler.handleFeignException(feignException);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        Problem problem = response.getBody();
        assertNotNull(problem);
        assertEquals(500, problem.getStatus());
        assertEquals("Internal Server Error", problem.getTitle());
        assertEquals(feignBody, problem.getDetail());
        assertEquals("/v1/users/onboarding", problem.getInstance());
    }

}