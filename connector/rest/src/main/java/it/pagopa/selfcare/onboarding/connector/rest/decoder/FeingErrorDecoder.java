package it.pagopa.selfcare.onboarding.connector.rest.decoder;

import feign.Response;
import feign.codec.ErrorDecoder;
import it.pagopa.selfcare.onboarding.connector.exceptions.ResourceNotFoundException;

public class FeingErrorDecoder extends ErrorDecoder.Default {

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 404) {
            throw new ResourceNotFoundException();
        } else {
            return super.decode(methodKey, response);
        }
    }
}
