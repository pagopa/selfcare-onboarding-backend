package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.connector.api.ProductsConnector;
import it.pagopa.selfcare.onboarding.connector.model.product.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductsConnector productsConnector;

    @Autowired
    public ProductServiceImpl(ProductsConnector productsConnector) {
        this.productsConnector = productsConnector;
    }

    @Override
    public Product getProduct(String id) {
        log.trace("getProduct start");
        log.debug("getProduct id = {}", id);
        Assert.notNull(id, "ProductId is required");
        Product product = productsConnector.getProduct(id);
        log.debug("getProduct result = {}", product);
        log.trace("getProduct end");
        return product;
    }
}
