package it.pagopa.selfcare.onboarding.connector.api;

import it.pagopa.selfcare.onboarding.connector.model.product.Product;

public interface ProductsConnector {

    Product getProduct(String id);

}
