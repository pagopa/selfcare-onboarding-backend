package it.pagopa.selfcare.onboarding.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.model.product.Product;
import it.pagopa.selfcare.onboarding.core.ProductService;
import it.pagopa.selfcare.onboarding.web.config.WebTestConfig;
import it.pagopa.selfcare.onboarding.web.model.ProductResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(value = {ProductController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {
        ProductController.class,
        WebTestConfig.class
})
class ProductControllerTest {
    private static final String BASE_URL = "/product";

    @Autowired
    protected MockMvc mvc;

    @MockBean
    protected ProductService productServiceMock;

    @Autowired
    protected ObjectMapper objectMapper;

    @Test
    void getProduct() throws Exception {
        //given
        String productId = "productId";
        Mockito.when(productServiceMock.getProduct(productId)).thenReturn(TestUtils.mockInstance(new Product()));
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        //then
        ProductResource response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ProductResource.class);
        Assertions.assertNotNull(response);
        Mockito.verify(productServiceMock, Mockito.times(1))
                .getProduct(productId);
        Mockito.verifyNoMoreInteractions(productServiceMock);
    }
}
