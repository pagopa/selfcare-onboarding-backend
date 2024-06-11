package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.common.InstitutionType;
import it.pagopa.selfcare.product.entity.Product;

public interface ProductService {

    Product getProduct(String id, InstitutionType institutionType);

    Product getProductValid(String id);
}
