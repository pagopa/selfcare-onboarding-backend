package it.pagopa.selfcare.onboarding.connector.rest.decoder;

import feign.FeignException;
import feign.Request;
import feign.Response;
import it.pagopa.selfcare.onboarding.connector.exceptions.InternalGatewayErrorException;
import it.pagopa.selfcare.onboarding.connector.exceptions.InvalidRequestException;
import it.pagopa.selfcare.onboarding.connector.exceptions.ResourceConflictException;
import it.pagopa.selfcare.onboarding.connector.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static feign.Util.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

class FeignErrorDecoderTest {

    FeignErrorDecoder feignDecoder = new FeignErrorDecoder();

    private Map<String, Collection<String>> headers = new LinkedHashMap<>();

    @Test
    void testDecodeInvalidRequest() throws Throwable {
        //given
        Response response = Response.builder()
                .status(400)
                .reason("InvalidRequest")
                .request(Request.create(Request.HttpMethod.GET, "/api", Collections.emptyMap(), null, UTF_8))
                .headers(headers)
                .body("hello world", UTF_8)
                .build();
        //when
        Executable executable = () -> feignDecoder.decode("", response);
        //then
        assertThrows(InvalidRequestException.class, executable);
    }

    @Test
    void testDecodeResourceConflict() throws Throwable {
        //given
        Response response = Response.builder()
                .status(409)
                .reason("ResourceConflict")
                .request(Request.create(Request.HttpMethod.POST, "/api", Collections.emptyMap(), null, UTF_8))
                .headers(headers)
                .body("hello world", UTF_8)
                .build();
        //when
        Executable executable = () -> feignDecoder.decode("", response);
        //then
        assertThrows(ResourceConflictException.class, executable);
    }

    @Test
    void testDecodeToResourceNotFound() throws Throwable {
        //given
        Response response = Response.builder()
                .status(404)
                .reason("ResourceNotFound")
                .request(Request.create(Request.HttpMethod.GET, "/api", Collections.emptyMap(), null, UTF_8))
                .headers(headers)
                .body("hello world", UTF_8)
                .build();
        //when
        Executable executable = () -> feignDecoder.decode("", response);
        //then
        assertThrows(ResourceNotFoundException.class, executable);
    }

    @Test
    void testDecodeToServerError() throws Throwable {
        //given
        Response response = Response.builder()
                .status(500)
                .reason("ResourceNotFound")
                .request(Request.create(Request.HttpMethod.GET, "/api", Collections.emptyMap(), null, UTF_8))
                .headers(headers)
                .build();
        //when
        Executable executable = () -> feignDecoder.decode("", response);
        //then
        assertThrows(InternalGatewayErrorException.class, executable);
    }

    @Test
    void testDecodeDefault() {
        //given
        Response response = Response.builder()
                .status(200)
                .reason("OK")
                .request(Request.create(Request.HttpMethod.GET, "/api", Collections.emptyMap(), null, UTF_8))
                .headers(headers)
                .body("hello world", UTF_8)
                .build();
        //when
        Executable executable = () -> feignDecoder.decode("", response);
        //then
        assertDoesNotThrow(executable);
    }

    @Test
    void testBypassDecoderFor_completeOnboardingUsingPUT() throws Exception {
        // given
        String methodKey = "MsOnboardingInternalApiClient#_completeOnboardingUsingPUT";

        Response response = Response.builder()
                .status(400)
                .reason("Bad Request")
                .request(Request.create(Request.HttpMethod.PUT, "/api", Collections.emptyMap(), null, UTF_8))
                .headers(headers)
                .body("{\"status\":400,\"error\":\"Bad request\"}", UTF_8)
                .build();

        // when
        Exception result = feignDecoder.decode(methodKey, response);

        // then
        assertTrue(result instanceof FeignException);
        assertEquals(400, ((FeignException) result).status());
    }

    @Test
    void testBypassDecoderFor_completeOnboardingUser() {
        // given
        String methodKey = "MsOnboardingApiClient#_completeOnboardingUser";

        Response response = Response.builder()
                .status(404)
                .reason("Not Found")
                .request(Request.create(Request.HttpMethod.PUT, "/api", Collections.emptyMap(), null, UTF_8))
                .headers(headers)
                .body("{\"status\":404,\"error\":\"Not found\"}", UTF_8)
                .build();

        // when
        Exception result = feignDecoder.decode(methodKey, response);

        // then
        assertTrue(result instanceof FeignException);
        assertEquals(404, ((FeignException) result).status());
    }

}