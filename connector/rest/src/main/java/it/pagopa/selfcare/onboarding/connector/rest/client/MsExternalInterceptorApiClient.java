package it.pagopa.selfcare.onboarding.connector.rest.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "${rest-client.ms-external-interceptor-api.serviceCode}", url = "${rest-client.ms-external-interceptor-api.baseUrl}")
public interface MsExternalInterceptorApiClient {
    @RequestMapping(method = RequestMethod.HEAD, value = "${rest-client.ms-external-interceptor-api.checkOrganization.path}")
    @ResponseBody
    void checkOrganization(@PathVariable("productId") String productId,
                           @RequestParam("fiscalCode") String fiscalCode,
                           @RequestParam("vatNumber") String vatNumber);
}
