package it.pagopa.selfcare.onboarding.connector.api;

import it.pagopa.selfcare.onboarding.common.InstitutionType;
import it.pagopa.selfcare.product.entity.Product;

public interface ProductsConnector {

    Product getProduct(String id, InstitutionType institutionType);

    Product getProductValid(String id);

}
