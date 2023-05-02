package it.pagopa.selfcare.onboarding.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.onboarding.connector.model.BusinessPnPG;
import it.pagopa.selfcare.onboarding.connector.model.PnPGInstitutionLegalAddressData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionPnPGInfo;
import it.pagopa.selfcare.onboarding.connector.model.institutions.PnPGMatchInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PnPGOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.onboarding.core.PnPGInstitutionService;
import it.pagopa.selfcare.onboarding.web.config.WebTestConfig;
import it.pagopa.selfcare.onboarding.web.model.InstitutionPnPGResource;
import it.pagopa.selfcare.onboarding.web.model.PnPGInstitutionLegalAddressResource;
import it.pagopa.selfcare.onboarding.web.model.PnPGMatchResource;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.emptyString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {PnPGInstitutionController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {PnPGInstitutionController.class, WebTestConfig.class})
class PnPGInstitutionControllerTest {

    private static final String BASE_URL = "/pnPGInstitutions";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private PnPGInstitutionService pnPGInstitutionServiceMock;


    @Test
    void getInstitutionsByUserId(@Value("classpath:stubs/userDto.json") Resource userDto) throws Exception {
        //given
        String userId = randomUUID().toString();
        List<BusinessPnPG> businessPnPGList = List.of(mockInstance(new BusinessPnPG()));
        InstitutionPnPGInfo institutionPnPGInfo = mockInstance(new InstitutionPnPGInfo(), "setBusinesses");
        institutionPnPGInfo.setBusinesses(businessPnPGList);
        User user = mockInstance(new User(), "setEmail", "setId", "setProductRole");
        user.setEmail("n.surname@email.com");
        when(pnPGInstitutionServiceMock.getInstitutionsByUser(Mockito.any()))
                .thenReturn(institutionPnPGInfo);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "")
                        .content(userDto.getInputStream().readAllBytes())
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        // then
        InstitutionPnPGResource response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(response);
        assertEquals(institutionPnPGInfo.getBusinesses().get(0).getBusinessName(), response.getBusinesses().get(0).getBusinessName());
        assertEquals(institutionPnPGInfo.getBusinesses().get(0).getBusinessTaxId(), response.getBusinesses().get(0).getBusinessTaxId());
        assertEquals(institutionPnPGInfo.getLegalTaxId(), response.getLegalTaxId());
        assertEquals(institutionPnPGInfo.getRequestDateTime(), response.getRequestDateTime());
        verify(pnPGInstitutionServiceMock, times(1))
                .getInstitutionsByUser(user);
        verifyNoMoreInteractions(pnPGInstitutionServiceMock);
    }

    @Test
    void onboarding(@Value("classpath:stubs/onboardingDto.json") Resource onboardingDto) throws Exception {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        // when
        mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/{institutionId}/products/{productId}/onboarding", institutionId, productId)
                        .content(onboardingDto.getInputStream().readAllBytes())
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(content().string(emptyString()));
        // then
        verify(pnPGInstitutionServiceMock, times(1))
                .onboarding(any(PnPGOnboardingData.class));
        verifyNoMoreInteractions(pnPGInstitutionServiceMock);
    }

    @Test
    void matchInstitutionAndUser_ok(@Value("classpath:stubs/userDto.json") Resource userDto) throws Exception {
        //given
        String externalInstitutionId = "externalId";
        PnPGMatchInfo pnPGMatchInfo = mockInstance(new PnPGMatchInfo(), "setVerificationResult");
        pnPGMatchInfo.setVerificationResult(true);
        User user = mockInstance(new User(), "setEmail", "setId", "setProductRole");
        user.setEmail("n.surname@email.com");
        when(pnPGInstitutionServiceMock.matchInstitutionAndUser(Mockito.anyString(), Mockito.any()))
                .thenReturn(pnPGMatchInfo);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/{externalInstitutionId}/match", externalInstitutionId)
                        .content(userDto.getInputStream().readAllBytes())
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        // then
        PnPGMatchResource response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(response);
        assertEquals(response.isVerificationResult(), pnPGMatchInfo.isVerificationResult());
        verify(pnPGInstitutionServiceMock, times(1))
                .matchInstitutionAndUser(externalInstitutionId, user);
        verifyNoMoreInteractions(pnPGInstitutionServiceMock);
    }

    @Test
    void getInstitutionLegalAddress() throws Exception {
        //given
        String externalInstitutionId = "externalId";
        PnPGInstitutionLegalAddressData data = mockInstance(new PnPGInstitutionLegalAddressData());
        when(pnPGInstitutionServiceMock.getInstitutionLegalAddress(Mockito.anyString()))
                .thenReturn(data);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{externalInstitutionId}/legal-address", externalInstitutionId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        // then
        PnPGInstitutionLegalAddressResource response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(response);
        assertEquals(response.getAddress(), data.getAddress());
        assertEquals(response.getZipCode(), data.getZipCode());
        verify(pnPGInstitutionServiceMock, times(1))
                .getInstitutionLegalAddress(externalInstitutionId);
        verifyNoMoreInteractions(pnPGInstitutionServiceMock);
    }

}