package it.pagopa.selfcare.onboarding.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Attribute;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.Billing;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
import it.pagopa.selfcare.onboarding.core.InstitutionService;
import it.pagopa.selfcare.onboarding.web.config.WebTestConfig;
import it.pagopa.selfcare.onboarding.web.model.BillingDataDto;
import it.pagopa.selfcare.onboarding.web.model.InstitutionOnboardingInfoResource;
import it.pagopa.selfcare.onboarding.web.model.InstitutionResource;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.emptyString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {InstitutionController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {InstitutionController.class, WebTestConfig.class})
class InstitutionControllerTest {

    private static final String BASE_URL = "/institutions";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private InstitutionService institutionServiceMock;


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
        verify(institutionServiceMock, times(1))
                .onboarding(any(OnboardingData.class));
        verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void getInstitutionOnboardingInfoResource() throws Exception {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        InstitutionInfo institutionInfoMock = mockInstance(new InstitutionInfo());
        institutionInfoMock.setBilling(mockInstance(new Billing()));
        InstitutionOnboardingData onBoardingDataMock = mockInstance(new InstitutionOnboardingData());
        onBoardingDataMock.setInstitution(institutionInfoMock);
        final UserInfo manager = mockInstance(new UserInfo(), "setId", "setInstitutionId");
        manager.setId(UUID.randomUUID().toString());
        manager.setInstitutionId(UUID.randomUUID().toString());
        onBoardingDataMock.setManager(manager);
        when(institutionServiceMock.getInstitutionOnboardingData(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(onBoardingDataMock);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/{institutionId}/products/{productId}/onboarded-institution-info", institutionId, productId)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        //then
        InstitutionOnboardingInfoResource response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                InstitutionOnboardingInfoResource.class
        );
        assertNotNull(response);
        assertNotNull(response.getInstitution());
        assertNotNull(response.getManager());
        BillingDataDto responseBillings = response.getInstitution().getBillingData();
        assertEquals(onBoardingDataMock.getInstitution().getBilling().getRecipientCode(), responseBillings.getRecipientCode());
        assertEquals(onBoardingDataMock.getInstitution().getBilling().getPublicServices(), responseBillings.getPublicServices());
        assertEquals(onBoardingDataMock.getInstitution().getBilling().getVatNumber(), responseBillings.getVatNumber());
        assertEquals(onBoardingDataMock.getInstitution().getDigitalAddress(), responseBillings.getDigitalAddress());
        assertEquals(onBoardingDataMock.getInstitution().getTaxCode(), responseBillings.getTaxCode());
        assertEquals(onBoardingDataMock.getInstitution().getAddress(), responseBillings.getRegisteredOffice());
        assertEquals(onBoardingDataMock.getInstitution().getInstitutionType(), response.getInstitution().getInstitutionType());
        assertNotNull(response.getManager().getId());
        verify(institutionServiceMock, times(1))
                .getInstitutionOnboardingData(institutionId, productId);
        verifyNoMoreInteractions(institutionServiceMock);

    }

    @Test
    void getInstitutionData() throws Exception {
        //given
        String institutionId = "institutionId";
        Institution institutionMock = mockInstance(new Institution(), "setId");
        institutionMock.setId(randomUUID().toString());
        Attribute attribute = mockInstance(new Attribute());
        institutionMock.setAttributes(List.of(attribute));
        when(institutionServiceMock.getInstitutionByExternalId(any()))
                .thenReturn(institutionMock);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/{institutionId}/data", institutionId)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        //then
        InstitutionResource response = objectMapper.readValue(result.getResponse().getContentAsString(), InstitutionResource.class);
        assertNotNull(response);
        verify(institutionServiceMock, times(1))
                .getInstitutionByExternalId(institutionId);
        verifyNoMoreInteractions(institutionServiceMock);
    }


    @Test
    void getInstitutions() throws Exception {
        //given
        InstitutionInfo institutionInfo = mockInstance(new InstitutionInfo(), "setId");
        institutionInfo.setId(randomUUID().toString());
        when(institutionServiceMock.getInstitutions())
                .thenReturn(Collections.singletonList(institutionInfo));
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "")
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        //then
        List<InstitutionResource> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(institutionInfo.getId(), response.get(0).getId().toString());
        assertEquals(institutionInfo.getExternalId(), response.get(0).getExternalId());
        assertEquals(institutionInfo.getDescription(), response.get(0).getDescription());
        verify(institutionServiceMock, times(1))
                .getInstitutions();
        verifyNoMoreInteractions(institutionServiceMock);
    }


    @Test
    void verifyOnboarding() throws Exception {
        final String externalInstitutionId = "externalInstitutionId";
        final String productId = "productId";
        //when
        mvc.perform(MockMvcRequestBuilders
                .head(BASE_URL + "/{externalInstitutionId}/products/{productId}", externalInstitutionId, productId)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());
    }

}