package it.pagopa.selfcare.onboarding.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import it.pagopa.selfcare.onboarding.common.InstitutionType;
import it.pagopa.selfcare.onboarding.core.ProductService;
import it.pagopa.selfcare.onboarding.web.model.ProductResource;
import it.pagopa.selfcare.onboarding.web.model.mapper.ProductMapper;
import it.pagopa.selfcare.product.entity.Product;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Api(tags = "product")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping(value = "/v1/product/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "${swagger.onboarding.product.api.getProduct}", operationId = "getProductUsingGET")
    public ProductResource getProduct(@ApiParam("${swagger.onboarding.product.model.id}")
                                      @PathVariable("id")
                                      String id,
                                      @ApiParam("${swagger.onboarding.institutions.model.institutionType}")
                                      @RequestParam(value = "institutionType", required = false)
                                      Optional<InstitutionType> institutionType) {
        log.trace("getProduct start");
        log.debug("getProduct id = {}, institutionType = {}", id, institutionType);
        Product product = productService.getProduct(id, institutionType.orElse(null));
        ProductResource resource = ProductMapper.toResource(product);
        log.debug("getProduct result = {}", resource);
        log.trace("getProduct end");
        return resource;
    }

    @GetMapping(value = "/v1/products",  produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "${swagger.onboarding.product.api.getProducts}",
            description = "${swagger.onboarding.product.api.getProducts}", operationId = "getProducts")
    public List<ProductResource> getProducts() {
        log.trace("getProducts start");
        final List<Product> products = productService.getProducts(false);
        List<ProductResource> resources = products.stream()
                .map(ProductMapper::toResource)
                .toList();
        log.debug("getProducts result = {}", resources);
        log.trace("getProducts end");
        return resources;
    }

    @GetMapping(value = "/v1/products/admin",  produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "${swagger.onboarding.product.api.getProductsAdmin}",
            description = "${swagger.onboarding.product.api.getProductsAdmin}", operationId = "getProductsAdmin")
    public List<ProductResource> getProductsAdmin() {
        log.trace("getProductsAdmin start");
        final List<Product> products = productService.getProducts(true);
        List<ProductResource> resources = products.stream()
                .filter(product -> Objects.nonNull(product.getUserContractTemplate(Product.CONTRACT_TYPE_DEFAULT).getContractTemplatePath()))
                .map(ProductMapper::toResource)
                .toList();
        log.debug("getProductsAdmin result = {}", resources);
        log.trace("getProductsAdmin end");
        return resources;
    }
}
