package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.product.entity.Product;
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
            resource.setLogo(model.getLogo());
            resource.setLogoBgColor(model.getLogoBgColor());
        }
        return resource;
    }

}
