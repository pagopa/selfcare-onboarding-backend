package it.pagopa.selfcare.onboarding.connector.rest.client;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.rest.config.PartyProcessRestClientTestConfig;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnboardingRequest;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnboardingResponse;
import it.pagopa.selfcare.onboarding.connector.rest.model.User;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
                "spring.application.name=selc-onboarding-connector-rest"
        })
@ContextConfiguration(
        initializers = PartyProcessRestClientTest.RandomPortInitializer.class,
        classes = {PartyProcessRestClientTestConfig.class})
public class PartyProcessRestClientTest extends BaseFeignRestClientTest {

    @ClassRule
    public static WireMockClassRule wireMockRule;

    static {
        WireMockConfiguration config = RestTestUtils.getWireMockConfiguration("stubs/party-process");
        wireMockRule = new WireMockClassRule(config);
    }


    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    String.format("USERVICE_PARTY_PROCESS_URL=http://%s:%d/pdnd-interop-uservice-party-process/0.0.1",
                            wireMockRule.getOptions().bindAddress(),
                            wireMockRule.port())
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
    public void onboardingOrganization_fullyValued() {
        // given
        OnboardingRequest onboardingRequest = new OnboardingRequest();
        onboardingRequest.setInstitutionId(testCase2instIdMap.get(TestCase.FULLY_VALUED));
        onboardingRequest.setUsers(List.of(TestUtils.mockInstance(new User())));
        // when
        OnboardingResponse response = restClient.onboardingOrganization(onboardingRequest);
        // then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getToken());
        Assert.assertNotNull(response.getDocument());
    }


    @Test
    public void onboardingOrganization_fullyNull() {
        // given
        OnboardingRequest onboardingRequest = new OnboardingRequest();
        onboardingRequest.setInstitutionId(testCase2instIdMap.get(TestCase.FULLY_NULL));
        onboardingRequest.setUsers(List.of(TestUtils.mockInstance(new User())));
        // when
        OnboardingResponse response = restClient.onboardingOrganization(onboardingRequest);
        // then
        Assert.assertNotNull(response);
        Assert.assertNull(response.getToken());
        Assert.assertNull(response.getDocument());
    }

}