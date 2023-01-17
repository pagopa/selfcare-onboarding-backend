package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.connector.api.PartyRegistryProxyConnector;
import it.pagopa.selfcare.onboarding.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.onboarding.connector.model.BusinessPnPG;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionPnPGInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PnPGInstitutionServiceImplTest {

    @InjectMocks
    private PnPGInstitutionServiceImpl pnPGInstitutionService;

    @Mock
    private PartyRegistryProxyConnector partyRegistryProxyConnector;

    @Mock
    private UserRegistryConnector userConnectorMock;

    @BeforeEach
    void beforeEach() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getInstitutionsByUserId_default() {
        //given
        String fiscalCode = "NoCF";
        List<BusinessPnPG> businessPnPGList = List.of(mockInstance(new BusinessPnPG()));
        InstitutionPnPGInfo institutionPnPGInfo = mockInstance(new InstitutionPnPGInfo(), "setBusinesses");
        institutionPnPGInfo.setBusinesses(businessPnPGList);
        when(partyRegistryProxyConnector.getInstitutionsByUserFiscalCode(fiscalCode))
                .thenReturn(institutionPnPGInfo);
        //when
        InstitutionPnPGInfo result = pnPGInstitutionService.getInstitutionsByUserId(fiscalCode);
        //then
        assertNotNull(result);
        assertEquals(institutionPnPGInfo.getBusinesses().get(0).getBusinessName(), result.getBusinesses().get(0).getBusinessName());
        assertEquals(institutionPnPGInfo.getBusinesses().get(0).getBusinessTaxId(), result.getBusinesses().get(0).getBusinessTaxId());
        assertEquals(institutionPnPGInfo.getLegalTaxId(), result.getLegalTaxId());
        assertEquals(institutionPnPGInfo.getRequestDateTime(), result.getRequestDateTime());
        verify(partyRegistryProxyConnector, times(1))
                .getInstitutionsByUserFiscalCode(fiscalCode);
        verifyNoMoreInteractions(partyRegistryProxyConnector);
        verifyNoInteractions(userConnectorMock);
    }

}