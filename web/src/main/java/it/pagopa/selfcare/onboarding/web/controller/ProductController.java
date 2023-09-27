package it.pagopa.selfcare.onboarding.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.onboarding.connector.model.product.Product;
import it.pagopa.selfcare.onboarding.core.ProductService;
import it.pagopa.selfcare.onboarding.web.model.ProductResource;
import it.pagopa.selfcare.onboarding.web.model.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@Slf4j
@RestController
@RequestMapping(value = "/product", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "product")
public class ProductController {


    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.onboarding.product.api.getProduct}")
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


}
