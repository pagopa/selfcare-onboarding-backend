package it.pagopa.selfcare.onboarding.connector.rest.decoder;

import feign.Response;
import feign.codec.ErrorDecoder;
import it.pagopa.selfcare.onboarding.connector.exceptions.InternalGatewayErrorException;
import it.pagopa.selfcare.onboarding.connector.exceptions.ResourceNotFoundException;

public class FeignErrorDecoder extends ErrorDecoder.Default {

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 404)
                throw new ResourceNotFoundException();
        if (response.status() >= 500 && response.status() < 599)
                throw new InternalGatewayErrorException();

        return super.decode(methodKey, response);
    }
}
