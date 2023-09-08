package it.pagopa.selfcare.onboarding.connector;

import it.pagopa.selfcare.onboarding.connector.api.MsExternalInterceptorConnector;
import it.pagopa.selfcare.onboarding.connector.rest.client.MsExternalInterceptorApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MsExternalInterceptorConnectorImpl implements MsExternalInterceptorConnector {

    private final MsExternalInterceptorApiClient restClient;

    public MsExternalInterceptorConnectorImpl(MsExternalInterceptorApiClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public void checkOrganization(String productId, String fiscalCode, String vatNumber) {
        log.trace("checkOrganization start");
        log.debug("checkOrganization productId = {}, fiscalCode = {}, vatNumber = {}", productId, fiscalCode, vatNumber );
        restClient.checkOrganization(productId, fiscalCode, vatNumber);
        log.trace("checkOrganization end");
    }
}
