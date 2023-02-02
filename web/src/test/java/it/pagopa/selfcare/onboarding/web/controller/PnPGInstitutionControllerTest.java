package it.pagopa.selfcare.onboarding.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.onboarding.core.PnPGInstitutionService;
import it.pagopa.selfcare.onboarding.web.config.WebTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

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


//    @Test
//    void getInstitutionsByUserId() throws Exception {
//        //given
//        String userId = randomUUID().toString();
//        List<BusinessPnPG> businessPnPGList = List.of(mockInstance(new BusinessPnPG()));
//        InstitutionPnPGInfo institutionPnPGInfo = mockInstance(new InstitutionPnPGInfo(), "setBusinesses");
//        institutionPnPGInfo.setBusinesses(businessPnPGList);
//        when(pnPGInstitutionServiceMock.getInstitutionsByUserId(Mockito.anyString()))
//                .thenReturn(institutionPnPGInfo);
//        //when
//        MvcResult result = mvc.perform(MockMvcRequestBuilders
//                        .get(BASE_URL + "/{userId}", userId)
//                        .contentType(APPLICATION_JSON_VALUE)
//                        .accept(APPLICATION_JSON_VALUE))
//                .andExpect(status().isOk())
//                .andReturn();
//        // then
//        InstitutionPnPGResource response = objectMapper.readValue(
//                result.getResponse().getContentAsString(),
//                new TypeReference<>() {
//                });
//        assertNotNull(response);
//        assertEquals(institutionPnPGInfo.getBusinesses().get(0).getBusinessName(), response.getBusinesses().get(0).getBusinessName());
//        assertEquals(institutionPnPGInfo.getBusinesses().get(0).getBusinessTaxId(), response.getBusinesses().get(0).getBusinessTaxId());
//        assertEquals(institutionPnPGInfo.getLegalTaxId(), response.getLegalTaxId());
//        assertEquals(institutionPnPGInfo.getRequestDateTime(), response.getRequestDateTime());
//        verify(pnPGInstitutionServiceMock, times(1))
//                .getInstitutionsByUserId(userId);
//        verifyNoMoreInteractions(pnPGInstitutionServiceMock);
//    }

}