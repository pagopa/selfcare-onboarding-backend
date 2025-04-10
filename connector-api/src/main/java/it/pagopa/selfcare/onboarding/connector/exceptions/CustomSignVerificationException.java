package it.pagopa.selfcare.onboarding.connector.exceptions;

public class CustomSignVerificationException extends RuntimeException {
    private final int status;
    private final String body;

    public CustomSignVerificationException(int status, String body) {
        super(body);
        this.status = status;
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }
}
