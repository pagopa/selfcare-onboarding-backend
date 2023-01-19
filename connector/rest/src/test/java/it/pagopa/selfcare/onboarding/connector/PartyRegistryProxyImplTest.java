package it.pagopa.selfcare.onboarding.connector;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.pagopa.selfcare.onboarding.connector.model.BusinessPnPG;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionPnPGInfo;
import it.pagopa.selfcare.onboarding.connector.rest.client.PartyRegistryProxyRestClient;
import it.pagopa.selfcare.onboarding.connector.rest.model.institution_pnpg.InstitutionByLegalTaxIdRequest;
import it.pagopa.selfcare.onboarding.connector.rest.model.institution_pnpg.InstitutionByLegalTaxIdRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.TimeZone;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartyRegistryProxyImplTest {

    @InjectMocks
    private PartyRegistryProxyConnectorImpl partyConnector;

    @Mock
    private PartyRegistryProxyRestClient restClientMock;

    @Captor
    ArgumentCaptor<InstitutionByLegalTaxIdRequest> institutionByLegalTaxIdRequestArgumentCaptor;

    private final ObjectMapper mapper;

    public PartyRegistryProxyImplTest() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setTimeZone(TimeZone.getDefault());
    }


    @Test
    void getInstitutionsByUserLegalTaxId() {
        // given
        String legalTaxId = "legalTaxId";
        List<BusinessPnPG> businessPnPGList = List.of(mockInstance(new BusinessPnPG()), mockInstance(new BusinessPnPG()));
        InstitutionPnPGInfo institutionPnPGInfo = mockInstance(new InstitutionPnPGInfo(), "setBusinesses");
        institutionPnPGInfo.setBusinesses(businessPnPGList);
        InstitutionByLegalTaxIdRequestDto institutionByLegalTaxIdRequestDto = new InstitutionByLegalTaxIdRequestDto();
        institutionByLegalTaxIdRequestDto.setLegalTaxId(legalTaxId);
        InstitutionByLegalTaxIdRequest institutionByLegalTaxIdRequest = new InstitutionByLegalTaxIdRequest();
        institutionByLegalTaxIdRequest.setFilter(institutionByLegalTaxIdRequestDto);
        when(restClientMock.getInstitutionsByUserLegalTaxId(any()))
                .thenReturn(institutionPnPGInfo);
        // when
        InstitutionPnPGInfo institutions = partyConnector.getInstitutionsByUserFiscalCode(legalTaxId);
        // then
        assertNotNull(institutions);
        assertNotNull(institutions.getRequestDateTime());
        assertNotNull(institutions.getLegalTaxId());
        assertNotNull(institutions.getBusinesses());
        assertEquals(2, institutions.getBusinesses().size());
        assertEquals(institutions.getRequestDateTime(), institutionPnPGInfo.getRequestDateTime());
        assertEquals(institutions.getLegalTaxId(), institutionPnPGInfo.getLegalTaxId());
        assertEquals(institutions.getBusinesses().get(0).getBusinessName(), institutionPnPGInfo.getBusinesses().get(0).getBusinessName());
        assertEquals(institutions.getBusinesses().get(0).getBusinessTaxId(), institutionPnPGInfo.getBusinesses().get(0).getBusinessTaxId());
        assertEquals(institutions.getBusinesses().get(1).getBusinessName(), institutionPnPGInfo.getBusinesses().get(1).getBusinessName());
        assertEquals(institutions.getBusinesses().get(1).getBusinessTaxId(), institutionPnPGInfo.getBusinesses().get(1).getBusinessTaxId());
        verify(restClientMock, times(1))
                .getInstitutionsByUserLegalTaxId(eq(institutionByLegalTaxIdRequest));
        verifyNoMoreInteractions(restClientMock);
    }


}