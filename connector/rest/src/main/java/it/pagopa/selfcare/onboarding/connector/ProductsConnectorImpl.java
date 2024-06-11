package it.pagopa.selfcare.onboarding.connector;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.onboarding.common.InstitutionType;
import it.pagopa.selfcare.onboarding.connector.api.ProductsConnector;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Objects;

@Slf4j
@Service
public class ProductsConnectorImpl implements ProductsConnector {
    private final ProductService productService;

    public ProductsConnectorImpl(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public Product getProduct(String id, InstitutionType institutionType) {
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getProduct id = {}", id);
        Assert.hasText(id, "A productId is required");
        Product product = productService.getProduct(id);
        if (Objects.nonNull(product.getInstitutionContractMappings()) && product.getInstitutionContractMappings().containsKey(institutionType)) {
            product.setContractTemplatePath(product.getInstitutionContractMappings().get(institutionType).getContractTemplatePath());
            product.setContractTemplateVersion(product.getInstitutionContractMappings().get(institutionType).getContractTemplateVersion());
            product.setContractTemplateUpdatedAt(product.getInstitutionContractMappings().get(institutionType).getContractTemplateUpdatedAt());
        }
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getProduct result = {}", product);
        return product;
    }
    @Override
    public Product getProductValid(String id) {
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getProductValid id = {}", id);
        Assert.hasText(id, "A productId is required");
        Product result = productService.getProductIsValid(id);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getProductValid result = {}", result);
        return result;
    }

}
