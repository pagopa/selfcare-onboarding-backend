package it.pagopa.selfcare.onboarding.connector.rest.decoder;

import feign.Response;
import feign.codec.ErrorDecoder;
import it.pagopa.selfcare.onboarding.connector.exceptions.InternalGatewayErrorException;
import it.pagopa.selfcare.onboarding.connector.exceptions.InvalidRequestException;
import it.pagopa.selfcare.onboarding.connector.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class FeignErrorDecoder extends ErrorDecoder.Default {

    @Override
    public Exception decode(String methodKey, Response response) {
        String errorMessage = Optional.ofNullable(response.body()).map(Object::toString).orElse(null);
        if (response.status() == 404)
                throw new ResourceNotFoundException();
        if (response.status() == 400)
                throw new InvalidRequestException(errorMessage);
        if (response.status() >= 500 && response.status() < 599) {
            log.error(errorMessage);
            throw new InternalGatewayErrorException();
        }

        return super.decode(methodKey, response);
    }
}
