package it.pagopa.selfcare.onboarding.core.exception;

public class TooManyResourceFoundException extends RuntimeException {

    public TooManyResourceFoundException(){}
    public TooManyResourceFoundException(String message) {
        super(message);
    }

}
