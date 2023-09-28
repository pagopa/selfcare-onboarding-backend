package it.pagopa.selfcare.onboarding.connector.rest.client;

import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.onboarding.connector.api.ProductsConnector;
import it.pagopa.selfcare.onboarding.connector.model.product.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(name = "${rest-client.products.serviceCode}", url = "${rest-client.products.base-url}")
public interface ProductsRestClient extends ProductsConnector {

    @GetMapping(value = "${rest-client.products.getProduct.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    Product getProduct(@PathVariable("id") String id,
                       @RequestParam(value = "institutionType", required = false) InstitutionType institutionType);

}
