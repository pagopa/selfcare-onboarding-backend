package it.pagopa.selfcare.onboarding.connector.exceptions;

public class InternalGatewayErrorException extends RuntimeException {
    public InternalGatewayErrorException() {
    }

    public InternalGatewayErrorException(String message) {
        super(message);
    }
}
