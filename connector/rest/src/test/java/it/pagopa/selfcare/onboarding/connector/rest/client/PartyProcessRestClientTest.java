package it.pagopa.selfcare.onboarding.connector.rest.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.rest.config.PartyProcessRestClientTestConfig;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnboardingRequest;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnboardingResponse;
import it.pagopa.selfcare.onboarding.connector.rest.model.User;
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

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@TestPropertySource(
        locations = "classpath:config/party-process-rest-client.properties",
        properties = {
                "logging.level.it.pagopa.selfcare.onboarding.connector.rest=DEBUG",
                "spring.application.name=selc-onboarding-connector-rest",
                "feign.okhttp.enabled=true"
        })
@ContextConfiguration(
        initializers = PartyProcessRestClientTest.RandomPortInitializer.class,
        classes = {PartyProcessRestClientTestConfig.class, HttpClientConfiguration.class})
class PartyProcessRestClientTest extends BaseFeignRestClientTest {

    @Order(1)
    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(RestTestUtils.getWireMockConfiguration("stubs/party-process"))
            .build();


    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    String.format("USERVICE_PARTY_PROCESS_URL=%s/pdnd-interop-uservice-party-process/0.0.1",
                            wm.getRuntimeInfo().getHttpBaseUrl())
            );
        }
    }


    private enum TestCase {
        FULLY_VALUED,
        FULLY_NULL,
        EMPTY_RESULT
    }

    private static final Map<TestCase, String> testCase2instIdMap = new EnumMap<>(TestCase.class) {{
        put(TestCase.FULLY_VALUED, "institutionId1");
        put(TestCase.FULLY_NULL, "institutionId2");
        put(TestCase.EMPTY_RESULT, "institutionId3");
    }};

    @Autowired
    private PartyProcessRestClient restClient;


    @Test
    void onboardingOrganization_fullyValued() {
        // given
        OnboardingRequest onboardingRequest = new OnboardingRequest();
        onboardingRequest.setInstitutionId(testCase2instIdMap.get(TestCase.FULLY_VALUED));
        onboardingRequest.setUsers(List.of(TestUtils.mockInstance(new User())));
        // when
        OnboardingResponse response = restClient.onboardingOrganization(onboardingRequest);
        // then
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getToken());
        Assertions.assertNotNull(response.getDocument());
    }


    @Test
    void onboardingOrganization_fullyNull() {
        // given
        OnboardingRequest onboardingRequest = new OnboardingRequest();
        onboardingRequest.setInstitutionId(testCase2instIdMap.get(TestCase.FULLY_NULL));
        onboardingRequest.setUsers(List.of(TestUtils.mockInstance(new User())));
        // when
        OnboardingResponse response = restClient.onboardingOrganization(onboardingRequest);
        // then
        Assertions.assertNotNull(response);
        Assertions.assertNull(response.getToken());
        Assertions.assertNull(response.getDocument());
    }

}