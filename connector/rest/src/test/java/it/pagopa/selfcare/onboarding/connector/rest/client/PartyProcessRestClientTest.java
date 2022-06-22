package it.pagopa.selfcare.onboarding.connector.rest.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipInfo;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipState;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipsResponse;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PartyRole;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.onboarding.connector.rest.config.PartyProcessRestClientTestConfig;
import it.pagopa.selfcare.onboarding.connector.rest.model.BillingDataResponse;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnBoardingInfo;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnboardingInstitutionRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.function.Executable;
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
        OnboardingInstitutionRequest onboardingRequest = new OnboardingInstitutionRequest();
        onboardingRequest.setInstitutionExternalId(testCase2instIdMap.get(TestCase.FULLY_VALUED));
        onboardingRequest.setUsers(List.of(TestUtils.mockInstance(new User())));
        // when
        Executable executable = () -> restClient.onboardingOrganization(onboardingRequest);
        // then
        assertDoesNotThrow(executable);
    }


    @Test
    void onboardingOrganization_fullyNull() {
        // given
        OnboardingInstitutionRequest onboardingRequest = new OnboardingInstitutionRequest();
        onboardingRequest.setInstitutionExternalId(testCase2instIdMap.get(TestCase.FULLY_NULL));
        onboardingRequest.setUsers(List.of(TestUtils.mockInstance(new User())));
        // when
        Executable executable = () -> restClient.onboardingOrganization(onboardingRequest);
        // then
        assertDoesNotThrow(executable);
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
        assertNotNull(response.get(0));
        response.forEach(relationshipInfo -> {
            TestUtils.checkNotNullFields(relationshipInfo);
            TestUtils.checkNotNullFields(relationshipInfo.getInstitutionUpdate());
            TestUtils.checkNotNullFields(relationshipInfo.getBilling());
        });
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
        response.getInstitutions().forEach(onboardingResponseData -> {
            TestUtils.checkNotNullFields(onboardingResponseData);
            TestUtils.checkNotNullFields(onboardingResponseData.getBilling());
            TestUtils.checkNotNullFields(onboardingResponseData.getProductInfo());
        });

    }


    @Test
    void getOnBoardingInfo_fullyNull() {
        // given and when
        OnBoardingInfo response = restClient.getOnBoardingInfo(testCase2instIdMap.get(TestCase.FULLY_NULL), EnumSet.of(ACTIVE));
        // then
        assertNotNull(response);
        assertNull(response.getUserId());
        assertNull(response.getInstitutions());
    }


    @Test
    void getOnBoardingInfo_emptyResult() {
        // given and when
        OnBoardingInfo response = restClient.getOnBoardingInfo(testCase2instIdMap.get(TestCase.EMPTY_RESULT), EnumSet.of(ACTIVE, PENDING));
        // then
        assertNotNull(response);
        assertTrue(response.getInstitutions().isEmpty());
        assertNull(response.getUserId());
    }

    @Test
    void getInstitutionByExternalId_fullyValued() {
        //given
        String externalId = testCase2instIdMap.get(TestCase.FULLY_VALUED);
        //when
        Institution response = restClient.getInstitutionByExternalId(externalId);
        //then
        assertNotNull(response);
        assertNotNull(response.getId());
        assertNotNull(response.getAddress());
    }

    @Test
    void getInstitutionByExternalId_fullyNull() {
        // given
        String externalId = testCase2instIdMap.get(TestCase.FULLY_NULL);
        // when
        Institution response = restClient.getInstitutionByExternalId(externalId);
        //then
        assertNotNull(response);
        assertNull(response.getAddress());
        assertNull(response.getDescription());
        assertNull(response.getDigitalAddress());
        assertNull(response.getId());
        assertNull(response.getExternalId());
        assertNull(response.getTaxCode());
        assertNull(response.getZipCode());
        assertNull(response.getOrigin());
        assertNull(response.getAttributes());
    }

    @Test
    void getInstitutionExternalId_notFound() {
        //given
        String externalId = "externalIdNotFound";
        //when
        Executable executable = () -> restClient.getInstitutionByExternalId(externalId);
        //then
        assertThrows(ResourceNotFoundException.class, executable);
    }

    @Test
    void createInstitutionUsingExternalId() {
        //given
        String externalId = "externalId";
        //when
        Institution response = restClient.createInstitutionUsingExternalId(externalId);
        //then
        assertNotNull(response);
        TestUtils.checkNotNullFields(response);
    }

    @Test
    void getInstitutionManager_fullyValued() {
        //given
        String externalId = "externalId1";
        String productId = "productId";
        //when
        RelationshipInfo response = restClient.getInstitutionManager(externalId, productId);
        //then
        assertNotNull(response);
        TestUtils.checkNotNullFields(response);
    }

    @Test
    void getInstitutionManager_fullyNull() {
        //given
        String externalId = "externalId2";
        String productId = "productId";
        //when
        RelationshipInfo response = restClient.getInstitutionManager(externalId, productId);
        //then
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getFrom());
        assertNull(response.getRole());
        assertNull(response.getState());
        assertNull(response.getBilling());
        assertNull(response.getPricingPlan());
        assertNull(response.getCreatedAt());
        assertNull(response.getInstitutionUpdate());
    }

    @Test
    void getInstitutionBillingData_fullyValued() {
        //given
        String externalId = "externalId1";
        String productId = "productId";
        //when
        BillingDataResponse response = restClient.getInstitutionBillingData(externalId, productId);
        //then
        assertNotNull(response);
        TestUtils.checkNotNullFields(response);
    }

    @Test
    void getInstitutionBillingData_fullyNull() {
        //given
        String externalId = "externalId2";
        String productId = "productId";
        //when
        BillingDataResponse response = restClient.getInstitutionBillingData(externalId, productId);
        //then
        assertNotNull(response);
        assertNull(response.getBilling());
        assertNull(response.getAddress());
        assertNull(response.getInstitutionId());
        assertNull(response.getInstitutionType());
        assertNull(response.getPricingPlan());
        assertNull(response.getExternalId());
    }

}