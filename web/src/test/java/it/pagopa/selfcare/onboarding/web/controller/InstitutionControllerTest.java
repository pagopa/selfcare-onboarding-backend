package it.pagopa.selfcare.onboarding.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.BillingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.core.InstitutionService;
import it.pagopa.selfcare.onboarding.web.config.WebTestConfig;
import it.pagopa.selfcare.onboarding.web.model.InstitutionResource;
import it.pagopa.selfcare.onboarding.web.model.OnboardingDto;
import it.pagopa.selfcare.onboarding.web.model.OnboardingResponse;
import it.pagopa.selfcare.onboarding.web.model.UserDto;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    @Captor
    private ArgumentCaptor<OnboardingData> onboardingDataCaptor;


    @Test
    void onboarding() throws Exception {
        File tempFile = File.createTempFile("hello", ".file");
        try {
            // given
            String institutionId = "institutionId";
            String productId = "productId";
            OnboardingResponse onboardingResponse = TestUtils.mockInstance(new OnboardingResponse(), "setDocument");
            onboardingResponse.setDocument(tempFile);
            List<UserDto> userDtos = List.of(TestUtils.mockInstance(new UserDto()));
            OnboardingDto onboardingDto = new OnboardingDto();
            BillingData billingData = TestUtils.mockInstance(new BillingData());
            onboardingDto.setUsers(userDtos);
//            onboardingDto.setBillingData(billingData);
            Mockito.when(institutionServiceMock.onboarding(Mockito.any()))
                    .thenReturn(onboardingResponse);
            // when
            MvcResult result = mvc.perform(MockMvcRequestBuilders
                    .post(BASE_URL + "/{institutionId}/products/{productId}/onboarding", institutionId, productId)
                    .content(objectMapper.writeValueAsString(onboardingDto))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .accept(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andReturn();
            // then
            OnboardingResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), OnboardingResponse.class);
            assertNotNull(response);
            assertNotNull(response.getToken());
            assertNotNull(response.getDocument());
            Mockito.verify(institutionServiceMock, Mockito.times(1))
                    .onboarding(onboardingDataCaptor.capture());
            OnboardingData captured = onboardingDataCaptor.getValue();
            assertNotNull(captured);
            assertEquals(institutionId, captured.getInstitutionId());
            assertEquals(productId, captured.getProductId());
            assertNotNull(captured.getUsers());
            assertEquals(1, captured.getUsers().size());
            assertEquals(userDtos.get(0).getName(), captured.getUsers().get(0).getName());
            assertEquals(userDtos.get(0).getSurname(), captured.getUsers().get(0).getSurname());
            assertEquals(userDtos.get(0).getTaxCode(), captured.getUsers().get(0).getTaxCode());
            assertEquals(userDtos.get(0).getEmail(), captured.getUsers().get(0).getEmail());
            assertEquals(userDtos.get(0).getRole(), captured.getUsers().get(0).getRole());
            assertEquals(userDtos.get(0).getProductRole(), captured.getUsers().get(0).getProductRole());
        } finally {
            tempFile.deleteOnExit();
        }
    }

    @Test
    void getInstitutions() throws Exception {
        //given
        InstitutionInfo institutionInfo = TestUtils.mockInstance(new InstitutionInfo());
        Mockito.when(institutionServiceMock.getInstitutions())
                .thenReturn(Collections.singletonList(institutionInfo));
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        //then
        List<InstitutionResource> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(institutionInfo.getInstitutionId(), response.get(0).getInstitutionId());
        assertEquals(institutionInfo.getDescription(), response.get(0).getDescription());
        Mockito.verify(institutionServiceMock, Mockito.times(1))
                .getInstitutions();
        Mockito.verifyNoMoreInteractions(institutionServiceMock);
    }

}