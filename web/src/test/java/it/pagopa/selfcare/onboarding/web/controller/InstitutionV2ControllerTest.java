package it.pagopa.selfcare.onboarding.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.core.InstitutionService;
import it.pagopa.selfcare.onboarding.web.config.WebTestConfig;
import it.pagopa.selfcare.onboarding.web.model.mapper.GeographicTaxonomyMapperImpl;
import it.pagopa.selfcare.onboarding.web.model.mapper.OnboardingInstitutionInfoMapperImpl;
import it.pagopa.selfcare.onboarding.web.model.mapper.OnboardingResourceMapperImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
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

import javax.validation.ValidationException;
import java.util.UUID;

import static org.hamcrest.Matchers.emptyString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(value = {InstitutionV2Controller.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {InstitutionV2Controller.class, WebTestConfig.class, OnboardingResourceMapperImpl.class, OnboardingInstitutionInfoMapperImpl.class, GeographicTaxonomyMapperImpl.class})
public class InstitutionV2ControllerTest {

    private static final String BASE_URL = "/v2/institutions";

    @Autowired
    protected MockMvc mvc;

    @MockBean
    private InstitutionService institutionServiceMock;

    @InjectMocks
    private InstitutionV2Controller institutionController;

    @Autowired
    protected ObjectMapper objectMapper;

    @Test
    void onboardingProductAsync(@Value("classpath:stubs/onboardingProductsDtoWithoutGeo.json") Resource onboardingDto) throws Exception {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        // when
        mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/onboarding", institutionId, productId)
                        .content(onboardingDto.getInputStream().readAllBytes())
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(content().string(emptyString()));
        // then
        verify(institutionServiceMock, times(1))
                .onboardingProductV2(any(OnboardingData.class));
        verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void onboardingProductForAggregatorAsync(@Value("classpath:stubs/onboardingProductsDtoWithAggregates.json") Resource onboardingDto) throws Exception {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        // when
        mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/onboarding", institutionId, productId)
                        .content(onboardingDto.getInputStream().readAllBytes())
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(content().string(emptyString()));
        // then
        verify(institutionServiceMock, times(1))
                .onboardingPaAggregator(any(OnboardingData.class));
        verifyNoMoreInteractions(institutionServiceMock);
    }


    @Test
    void onboardingCompany(@Value("classpath:stubs/onboardingCompanyDto.json") Resource onboardingDto) throws Exception {

        // when
        mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/company/onboarding")
                        .content(onboardingDto.getInputStream().readAllBytes())
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(content().string(emptyString()));
        // then

        verify(institutionServiceMock, times(1))
                .onboardingCompanyV2(any(OnboardingData.class));
        verifyNoMoreInteractions(institutionServiceMock);
    }

    /**
     * Method under test: {@link InstitutionV2Controller#getInstitution(String, String, String, String, String)}
     */
    @Test
    void getByFilters() throws Exception {

        // when
        final String origin = "origin";
        final String originId = "originId";
        final String productId = "productId";
        Institution institution = new Institution();
        institution.setDescription("description");
        institution.setId(UUID.randomUUID().toString());
        when(institutionServiceMock.getByFilters(productId, null, origin, originId, null))
                .thenReturn(institution);
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "?origin={origin}&originId={originId}&productId={productId}", origin, originId, productId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        // then
        Institution response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                Institution.class);
        assertNotNull(response);
        assertEquals(institution.getDescription(), response.getDescription());
        assertEquals(institution.getId(), response.getId());
    }

    /**
     * Method under test: {@link InstitutionV2Controller#getInstitution(String, String, String, String, String)}
     */
    @Test
    void getByFiltersBadRequest() throws Exception {
        // when
        final String origin = "origin";
        final String originId = "originId";
        // when
        mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "?origin={origin}&originId={originId}", origin, originId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest());

    }

    /**
     * Method under test: {@link InstitutionV2Controller#getInstitution(String, String, String, String, String)}
     */
    @Test
    void getByFiltersValidationException(){
        Assertions.assertThrows(ValidationException.class,
                () -> institutionController.getInstitution(null, null, null, null, null),
                "At least one of taxCode, origin or originId must be present");
    }
}
