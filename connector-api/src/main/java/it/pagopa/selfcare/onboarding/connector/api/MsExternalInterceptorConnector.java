package it.pagopa.selfcare.onboarding.connector.api;

public interface MsExternalInterceptorConnector {
    void checkOrganization(String productId, String fiscalCode, String vatNumber);
}
