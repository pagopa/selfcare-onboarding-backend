package it.pagopa.selfcare.onboarding.connector.rest.client;

import it.pagopa.selfcare.onboarding.connector.api.ProductsConnector;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PartyRole;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.EnumMap;
import java.util.List;

@FeignClient(name = "${rest-client.products.serviceCode}", url = "${rest-client.products.base-url}")
public interface ProductsRestClient extends ProductsConnector {//TODO: write unit tests

    @GetMapping(value = "${rest-client.products.getProductRoleMappings.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    EnumMap<PartyRole, List<String>> getProductRoleMappings(@PathVariable("id") String id);
}
