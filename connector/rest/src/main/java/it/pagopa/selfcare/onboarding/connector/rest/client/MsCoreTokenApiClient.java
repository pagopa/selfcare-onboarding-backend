package it.pagopa.selfcare.onboarding.connector.rest.client;

import it.pagopa.selfcare.core.generated.openapi.v1.api.TokenApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Ms Core Rest Client
 */
@FeignClient(name = "${rest-client.ms-core-token-api.serviceCode}", url = "${rest-client.ms-core.base-url}")
public interface MsCoreTokenApiClient extends TokenApi {
}
