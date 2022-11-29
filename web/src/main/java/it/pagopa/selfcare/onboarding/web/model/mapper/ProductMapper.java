package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.product.Product;
import it.pagopa.selfcare.onboarding.web.model.ProductResource;

public class ProductMapper {

    public static ProductResource toResource(Product model) {
        ProductResource resource = null;
        if (model != null) {
            resource = new ProductResource();
            resource.setId(model.getId());
            resource.setTitle(model.getTitle());
            resource.setParentId(model.getParentId());
            resource.setStatus(model.getStatus());
        }
        return resource;
    }

}
