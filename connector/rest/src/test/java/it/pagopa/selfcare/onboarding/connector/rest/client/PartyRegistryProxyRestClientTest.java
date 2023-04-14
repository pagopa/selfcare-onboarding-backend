package it.pagopa.selfcare.onboarding.connector.rest.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.onboarding.connector.model.PnPGInstitutionLegalAddressData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionPnPGInfo;
import it.pagopa.selfcare.onboarding.connector.model.institutions.PnPGMatchInfo;
import it.pagopa.selfcare.onboarding.connector.rest.config.PartyRegistryProxyRestClientTestConfig;
import it.pagopa.selfcare.onboarding.connector.rest.model.institution_pnpg.InstitutionByLegalTaxIdRequest;
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

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.*;

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
                    String.format("USERVICE_PARTY_REGISTRY_PROXY_URL:%s/external/ur/v1",
                            wm.getRuntimeInfo().getHttpBaseUrl())
            );
        }
    }

    @Autowired
    private PartyRegistryProxyRestClient restClient;

    @Test
    void getInstitutionsByUserLegalTaxId_fullyValued() {
        //given
        InstitutionByLegalTaxIdRequest request = mockInstance(new InstitutionByLegalTaxIdRequest());
        //when
        InstitutionPnPGInfo response = restClient.getInstitutionsByUserLegalTaxId(request);
        //then
        assertNotNull(response);
        assertNotNull(response.getRequestDateTime());
        assertNotNull(response.getLegalTaxId());
        assertNotNull(response.getBusinesses());
        assertNotNull(response.getBusinesses().get(0).getBusinessName());
        assertNotNull(response.getBusinesses().get(0).getBusinessTaxId());
    }

    @Test
    void getInstitutionsByUserLegalTaxId_fullyNull() {
        // given
        InstitutionByLegalTaxIdRequest request = mockInstance(new InstitutionByLegalTaxIdRequest());
        // when
        Executable executable = () -> restClient.getInstitutionsByUserLegalTaxId(request);
        // then
        assertDoesNotThrow(executable);
    }

    @Test
    void matchInstitutionAndUser_checkTrue() {
        //given
        String institutionExternalId = "instituionId";
        String taxCode = "taxCode";
        //when
        PnPGMatchInfo response = restClient.matchInstitutionAndUser(institutionExternalId, taxCode);
        //then
        assertNotNull(response);
        assertTrue(response.isVerificationResult());
    }

    @Test
    void matchInstitutionAndUser_checkFalse() {
        //given
        String institutionExternalId = "instituionId";
        String taxCode = "taxCode";
        //when
        PnPGMatchInfo response = restClient.matchInstitutionAndUser(institutionExternalId, taxCode);
        //then
        assertNotNull(response);
        assertTrue(response.isVerificationResult());
    }

    @Test
    void getInstitutionLegalAddress_fullyValued() {
        //given
        String institutionExternalId = "extInstituionId";
        //when
        PnPGInstitutionLegalAddressData response = restClient.getInstitutionLegalAddress(institutionExternalId);
        //then
        assertNotNull(response);
        assertNotNull(response.getAddress());
        assertNotNull(response.getZipCode());
    }

    @Test
    void getInstitutionLegalAddress_fullyNull() {
        //given
        String institutionExternalId = "extInstituionId";
        //when
        Executable executable = () -> restClient.getInstitutionLegalAddress(institutionExternalId);
        //then
        assertDoesNotThrow(executable);
    }
}