package it.pagopa.selfcare.onboarding.connector.rest.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipState;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipsResponse;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PartyRole;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.onboarding.connector.rest.config.PartyProcessRestClientTestConfig;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnBoardingInfo;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnboardingRequest;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnboardingResponse;
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

import java.util.*;

import static it.pagopa.selfcare.onboarding.connector.model.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.onboarding.connector.model.RelationshipState.PENDING;
import static it.pagopa.selfcare.onboarding.connector.model.onboarding.PartyRole.MANAGER;
import static it.pagopa.selfcare.onboarding.connector.model.onboarding.PartyRole.OPERATOR;
import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void getUserInstitutionRelationships_fullyValued() {
        // given
        String institutionId = testCase2instIdMap.get(TestCase.FULLY_VALUED);
        EnumSet<PartyRole> roles = null;
        EnumSet<RelationshipState> states = EnumSet.of(ACTIVE);
        Set<String> products = Set.of("productId");
        Set<String> productRole = null;
        String userId = null;
        // when
        RelationshipsResponse response = restClient.getUserInstitutionRelationships(institutionId, roles, states, products, productRole, userId);
        // then
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertNotNull(response.get(0).getId());
        assertNotNull(response.get(0).getFrom());
        assertNotNull(response.get(0).getTo());
        assertNotNull(response.get(0).getName());
        assertNotNull(response.get(0).getSurname());
        assertNotNull(response.get(0).getEmail());
        assertNotNull(response.get(0).getRole());
        assertNotNull(response.get(0).getState());
        assertNotNull(response.get(0).getCreatedAt());
        assertNotNull(response.get(0).getUpdatedAt());
    }

    @Test
    void getInstitutionRelationships_fullyNull() {
        // given
        String institutionId = testCase2instIdMap.get(TestCase.FULLY_NULL);
        EnumSet<PartyRole> roles = null;
        EnumSet<RelationshipState> states = null;
        Set<String> products = null;
        Set<String> productRole = null;
        String userId = null;
        // when
        RelationshipsResponse response = restClient.getUserInstitutionRelationships(institutionId, roles, states, products, productRole, userId);
        // then
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertNull(response.get(0).getId());
        assertNull(response.get(0).getFrom());
        assertNull(response.get(0).getRole());
        assertNull(response.get(0).getState());
    }

    @Test
    void getInstitutionRelationships_emptyResult() {
        // given
        String institutionId = testCase2instIdMap.get(TestCase.EMPTY_RESULT);
        EnumSet<PartyRole> roles = EnumSet.of(MANAGER, OPERATOR);
        EnumSet<RelationshipState> states = EnumSet.of(ACTIVE, PENDING);
        Set<String> products = Set.of("prod1", "prod2");
        Set<String> productRole = Set.of("api", "security");
        String userId = "userId";
        // when
        RelationshipsResponse response = restClient.getUserInstitutionRelationships(institutionId, roles, states, products, productRole, userId);

        // then
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }


    @Test
    void getOnBoardingInfo_fullyValued() {
        // given and when
        OnBoardingInfo response = restClient.getOnBoardingInfo(testCase2instIdMap.get(TestCase.FULLY_VALUED), null);
        // then
        assertNotNull(response);
        assertNotNull(response.getPerson());
        assertNotNull(response.getInstitutions());
        assertNotNull(response.getPerson().getName());
        assertNotNull(response.getPerson().getSurname());
        assertNotNull(response.getPerson().getTaxCode());
        assertNotNull(response.getInstitutions().get(0).getInstitutionId());
        assertNotNull(response.getInstitutions().get(0).getDescription());
    }


    @Test
    void getOnBoardingInfo_fullyNull() {
        // given and when
        OnBoardingInfo response = restClient.getOnBoardingInfo(testCase2instIdMap.get(TestCase.FULLY_NULL), EnumSet.of(ACTIVE));
        // then
        assertNotNull(response);
        assertNotNull(response.getPerson());
        assertNotNull(response.getInstitutions());
        assertNull(response.getPerson().getName());
        assertNull(response.getPerson().getSurname());
        assertNull(response.getPerson().getTaxCode());
        assertNull(response.getInstitutions().get(0).getInstitutionId());
        assertNull(response.getInstitutions().get(0).getDescription());
    }


    @Test
    void getOnBoardingInfo_emptyResult() {
        // given and when
        OnBoardingInfo response = restClient.getOnBoardingInfo(testCase2instIdMap.get(TestCase.EMPTY_RESULT), EnumSet.of(ACTIVE, PENDING));
        // then
        assertNotNull(response);
        assertTrue(response.getInstitutions().isEmpty());
        assertNull(response.getPerson());
    }


}