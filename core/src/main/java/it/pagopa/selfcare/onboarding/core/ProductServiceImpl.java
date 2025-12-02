package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.connector.api.ProductMsConnector;
import it.pagopa.selfcare.onboarding.connector.model.product.OriginResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    // CONNECTOR
    private final ProductMsConnector productMsConnector;

    public ProductServiceImpl(ProductMsConnector productMsConnector) {
        this.productMsConnector = productMsConnector;
    }

    @Override
    public OriginResult getOrigins(String productId) {
        log.trace("getOrigins start");
        OriginResult originResult = productMsConnector.getOrigins(productId);
        log.debug("getOrigins size = {}", originResult.getOrigins().size());
        log.trace("getOrigins end");
        return originResult;
    }
}
