package it.pagopa.selfcare.onboarding.web.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionUpdate;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.core.TokenService;
import it.pagopa.selfcare.onboarding.web.config.WebTestConfig;
import it.pagopa.selfcare.onboarding.web.model.OnboardingRequestResource;
import it.pagopa.selfcare.onboarding.web.model.mapper.OnboardingResourceMapperImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {TokenV2Controller.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {TokenV2Controller.class, WebTestConfig.class, OnboardingResourceMapperImpl.class})
public class TokenV2ControllerTest {

    @Autowired
    protected MockMvc mvc;

    @MockBean
    private TokenService tokenService;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Method under test: {@link TokenV2Controller#complete(String, MultipartFile)}
     */
    @Test
    void shouldCompleteToken() throws Exception {

        MockMultipartFile file = new MockMultipartFile("contract", "".getBytes());
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/v2/tokens/{tokenId}/complete",
                        "42")
                .file(file);
        mvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    /**
     * Method under test: {@link TokenV2Controller#verifyOnboarding(String)}
     */
    @Test
    void verifyOnboarding() throws Exception {

        String onboardingId = UUID.randomUUID().toString();
        doNothing().when(tokenService).verifyOnboarding(onboardingId);

        //when
        mvc.perform(MockMvcRequestBuilders
                        .post("/v2/tokens/{onboardingId}/verify", onboardingId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
        //then
        verify(tokenService, times(1))
                .verifyOnboarding(onboardingId);
    }

    /**
     * Method under test: {@link TokenV2Controller#retrieveOnboardingRequest(String)}
     */
    @Test
    void retrieveOnboardingRequest() throws Exception {

        OnboardingData onboardingData = new OnboardingData();
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setTaxCode("taxCode");
        onboardingData.setInstitutionUpdate(institutionUpdate);

        String onboardingId = UUID.randomUUID().toString();
        when(tokenService.getOnboardingWithUserInfo(onboardingId))
                .thenReturn(onboardingData);

        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get("/v2/tokens/{onboardingId}", onboardingId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        OnboardingRequestResource response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                OnboardingRequestResource.class
        );
        //then

        assertEquals(institutionUpdate.getTaxCode(), response.getInstitutionInfo().getFiscalCode());

        verify(tokenService, times(1))
                .getOnboardingWithUserInfo(onboardingId);
    }
}
