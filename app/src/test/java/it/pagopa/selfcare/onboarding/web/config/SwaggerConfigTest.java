package it.pagopa.selfcare.onboarding.web.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.web.config.SecurityConfig;
import it.pagopa.selfcare.onboarding.connector.api.ProductsConnector;
import it.pagopa.selfcare.onboarding.connector.rest.client.*;
import it.pagopa.selfcare.onboarding.connector.rest.config.FeignClientConfig;
import it.pagopa.selfcare.onboarding.connector.rest.config.ProductServiceConfig;
import it.pagopa.selfcare.onboarding.connector.rest.config.UserRegistryRestClientConfig;
import it.pagopa.selfcare.onboarding.core.InstitutionService;
import it.pagopa.selfcare.onboarding.core.ProductService;
import it.pagopa.selfcare.onboarding.core.TokenService;
import it.pagopa.selfcare.onboarding.core.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ComponentScan(basePackages = {
        "it.pagopa.selfcare.onboarding.web.controller",
        "it.pagopa.selfcare.onboarding.web.model.mapper",
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:config/application.yml")
class SwaggerConfigTest {

    @MockBean
    private InstitutionService institutionService;

    @MockBean
    private ProductService productService;

    @MockBean
    private ProductsConnector productsConnector;

    @MockBean
    private UserService userService;

    @MockBean
    private TokenService tokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRegistryRestClient userRegistryRestClient;

    @MockBean
    private PartyProcessRestClient partyProcessRestClient;

    @MockBean
    private OnboardingFunctionsApiClient onboardingFunctionsApiClient;

    @MockBean
    private MsUserApiClient msUserApiClient;

    @MockBean
    private MsOnboardingTokenApiClient MsOnboardingTokenApiClient;

    @MockBean
    private MsOnboardingAggregatesApiClient msOnboardingAggregatesApiClient;

    @MockBean
    private MsOnboardingSupportApiClient msOnboardingSupportApiClient;

    @MockBean
    private MsOnboardingApiClient msOnboardingApiClient;

    @MockBean
    private MsCoreRestClient msCoreRestClient;

    @MockBean
    private PartyRegistryProxyRestClient partyRegistryProxyRestClient;

    @MockBean
    private UserRegistryRestClientConfig userRegistryRestClientConfig;

    @MockBean
    private ProductServiceConfig productServiceConfig;

    @Autowired
    WebApplicationContext context;

    @Test
    void testApiDocsEndpoint() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        String response = mockMvc.perform(MockMvcRequestBuilders.get("/v3/api-docs")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertNotNull(response);
        assertFalse(response.isBlank());
        assertFalse(response.contains("${"), "Generated swagger contains placeholders");

        Object swagger = objectMapper.readValue(response, Object.class);
        assertNotNull(swagger);

        String formatted = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(swagger);
        System.out.println(formatted);
    }
}

