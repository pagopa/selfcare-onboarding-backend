package it.pagopa.selfcare.onboarding.connector.rest.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.onboarding.connector.model.BusinessPnPG;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionPnPGInfo;
import it.pagopa.selfcare.onboarding.connector.rest.config.PartyRegistryProxyRestClientTestConfig;
import it.pagopa.selfcare.onboarding.connector.rest.model.institution_pnpg.InstitutionByLegalTaxIdRequest;
import it.pagopa.selfcare.onboarding.connector.rest.model.institution_pnpg.InstitutionByLegalTaxIdRequestDto;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.commons.httpclient.HttpClientConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.util.List;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestPropertySource(
        locations = "classpath:config/party-registry-proxy-rest-client.properties",
        properties = {
                "logging.level.it.pagopa.selfcare.onboarding.connector.rest=DEBUG",
                "spring.application.name=selc-onboarding-connector-rest",
                "feign.okhttp.enabled=true"
        })
@ContextConfiguration(
        initializers = PartyRegistryProxyRestClientTest.RandomPortInitializer.class,
        classes = {PartyRegistryProxyRestClientTestConfig.class, HttpClientConfiguration.class})
class PartyRegistryProxyRestClientTest extends BaseFeignRestClientTest {

    @Order(1)
    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(RestTestUtils.getWireMockConfiguration("stubs/party-registry-proxy"))
            .build();


    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    String.format("USERVICE_PARTY_REGISTRY_PROXY_URL=%s/external/ur",
                            wm.getRuntimeInfo().getHttpBaseUrl())
            );
        }
    }

    @MockBean
    private PartyRegistryProxyRestClient restClient;


    @Test
    void getInstitutionsByUserLegalTaxId_fullyValued() {
        //given
        String legalTaxId = "legalTaxId";
        List<BusinessPnPG> businessPnPGList = List.of(mockInstance(new BusinessPnPG()), mockInstance(new BusinessPnPG()));
        InstitutionPnPGInfo institutionPnPGInfo = mockInstance(new InstitutionPnPGInfo(), "setBusinesses");
        institutionPnPGInfo.setBusinesses(businessPnPGList);
        InstitutionByLegalTaxIdRequestDto institutionByLegalTaxIdRequestDto = new InstitutionByLegalTaxIdRequestDto();
        institutionByLegalTaxIdRequestDto.setLegalTaxId(legalTaxId);
        InstitutionByLegalTaxIdRequest institutionByLegalTaxIdRequest = new InstitutionByLegalTaxIdRequest();
        institutionByLegalTaxIdRequest.setFilter(institutionByLegalTaxIdRequestDto);
        when(restClient.getInstitutionsByUserLegalTaxId(any()))
                .thenReturn(institutionPnPGInfo);
        //when
        InstitutionPnPGInfo response = restClient.getInstitutionsByUserLegalTaxId(institutionByLegalTaxIdRequest);
        //then
        assertNotNull(response);
        assertNotNull(response.getLegalTaxId());
        assertNotNull(response.getRequestDateTime());
        assertNotNull(response.getBusinesses());
        assertNotNull(response.getBusinesses().get(0).getBusinessTaxId());
        assertNotNull(response.getBusinesses().get(0).getBusinessName());
        assertNotNull(response.getBusinesses().get(1).getBusinessTaxId());
        assertNotNull(response.getBusinesses().get(1).getBusinessName());
    }

/*

    @Test
    void getInstitutionsByUserLegalTaxId_fullyNull() {
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
    void getInstitutionsByUserLegalTaxId_emptyResult() {
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
*/

}