package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.onboarding.web.model.ProductResource;
import it.pagopa.selfcare.product.entity.ProductStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProductMapperTest {

    @Test
    void toResource_notNull() {
        //given
        Product product = new Product();
        product.setId("id");
        product.setTitle("title");
        product.setStatus(ProductStatus.ACTIVE);
        // when
        ProductResource productResource = ProductMapper.toResource(product);
        // then
        assertEquals(product.getId(), productResource.getId());
        assertEquals(product.getTitle(), productResource.getTitle());
        assertEquals(product.getStatus(), productResource.getStatus());

        TestUtils.reflectionEqualsByName(productResource, product);
    }

    @Test
    void toResource_null() {
        // given
        // when
        ProductResource productResource = ProductMapper.toResource(null);
        // then
        assertNull(productResource);
    }


}