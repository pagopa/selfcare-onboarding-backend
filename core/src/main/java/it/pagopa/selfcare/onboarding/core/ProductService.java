package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.connector.model.product.Product;

public interface ProductService {

    Product getProduct(String id);
}
