package it.pagopa.selfcare.onboarding.connector.rest.decoder;

import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import it.pagopa.selfcare.onboarding.connector.exceptions.ResourceConflictException;
import it.pagopa.selfcare.onboarding.connector.exceptions.InternalGatewayErrorException;
import it.pagopa.selfcare.onboarding.connector.exceptions.InvalidRequestException;
import it.pagopa.selfcare.onboarding.connector.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class FeignErrorDecoder extends ErrorDecoder.Default {

    @Override
    public Exception decode(String methodKey, Response response) {
        String errorMessage = null;

        if (methodKey.contains("MsOnboardingInternalApiClient#_completeOnboardingUsingPUT")
                || methodKey.contains("MsOnboardingApiClient#_completeOnboardingUser")) {
            return FeignException.errorStatus(methodKey, response);
        }

        if (response.body() != null) {
            try (InputStream inputStream = response.body().asInputStream()) {
                errorMessage = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.warn("Failed to read Feign response body", e);
            }
        }

        if (response.status() == 404)
            throw new ResourceNotFoundException(errorMessage);

        if (response.status() == 400)
            throw new InvalidRequestException(errorMessage);

        if (response.status() == 409)
            throw new ResourceConflictException(errorMessage);

        if (response.status() >= 500 && response.status() < 599) {
            log.error(errorMessage);
            throw new InternalGatewayErrorException(errorMessage);
        }

        return super.decode(methodKey, response);
    }
}
