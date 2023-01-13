package it.pagopa.selfcare.onboarding.connector.rest.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionType;
import it.pagopa.selfcare.onboarding.connector.model.product.Product;
import it.pagopa.selfcare.onboarding.connector.rest.config.ProductsRestClientTestConfig;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.commons.httpclient.HttpClientConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

@TestPropertySource(
        locations = "classpath:config/products-rest-client.properties",
        properties = {
                "logging.level.it.pagopa.selfcare.onboarding.connector.rest=DEBUG",
                "spring.application.name=selc-onboarding-connector-rest",
                "feign.okhttp.enabled=true"
        })
@ContextConfiguration(
        initializers = ProductsRestClientTest.RandomPortInitializer.class,
        classes = {ProductsRestClientTestConfig.class, HttpClientConfiguration.class})
class ProductsRestClientTest extends BaseFeignRestClientTest {

    @Order(1)
    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(RestTestUtils.getWireMockConfiguration("stubs/products"))
            .build();

    @Autowired
    private ProductsRestClient restClient;


    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    String.format("MS_PRODUCT_URL=%s",
                            wm.getRuntimeInfo().getHttpBaseUrl())
            );
        }
    }

    @Test
    void getProduct_institutionTypeNull() {
        // given
        String id = "id";
        // when
        Product product = restClient.getProduct(id, null);
        // then
        Assertions.assertNotNull(product);
        Assertions.assertNotNull(product.getId());
        Assertions.assertNotNull(product.getContractTemplatePath());
        Assertions.assertNotNull(product.getContractTemplateVersion());
        Assertions.assertNotNull(product.getRoleMappings());
        Assertions.assertNotNull(product.getTitle());
        Assertions.assertFalse(product.getRoleMappings().isEmpty());
    }

    @Test
    void getProduct_institutionTypeNotNull() {
        // given
        String id = "id";
        InstitutionType institutionType = InstitutionType.PA;
        // when
        Product product = restClient.getProduct(id, institutionType);
        // then
        Assertions.assertNotNull(product);
        Assertions.assertNotNull(product.getId());
        Assertions.assertNotNull(product.getContractTemplatePath());
        Assertions.assertNotNull(product.getContractTemplateVersion());
        Assertions.assertNotNull(product.getRoleMappings());
        Assertions.assertNotNull(product.getTitle());
        Assertions.assertFalse(product.getRoleMappings().isEmpty());
    }

}