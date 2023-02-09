package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.connector.api.PartyRegistryProxyConnector;
import it.pagopa.selfcare.onboarding.connector.api.UserRegistryConnector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

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

    // fixme: doesn't work
    /*@Test
    void getInstitutionsByUser_emptyResult() {
        //given
        //when
        Collection<InstitutionInfo> institutions = institutionService.getInstitutions();
        // then
        assertNotNull(institutions);
        assertTrue(institutions.isEmpty());
        verify(partyConnectorMock, times(1))
                .getOnBoardedInstitutions();
        verifyNoMoreInteractions(partyConnectorMock);
        verifyNoInteractions(productsConnectorMock, userConnectorMock);
    }*/

    // fixme: doesn't work
   /* @Test
    void getInstitutionsByUser_default() {
        //given
        String fiscalCode = "NoCF";
        UserId userId = new UserId();
        User user = mockInstance(new User(), "setId");
        user.setId(userId.toString());
        SaveUserDto saveUserDto = mockInstance(new SaveUserDto(), "setFiscalCode", "setWorkContacts");
        saveUserDto.setFiscalCode("setTaxCode");

        List<BusinessPnPG> businessPnPGList = List.of(mockInstance(new BusinessPnPG()));
        InstitutionPnPGInfo institutionPnPGInfo = mockInstance(new InstitutionPnPGInfo(), "setBusinesses", "setLegalTaxId");
        institutionPnPGInfo.setBusinesses(businessPnPGList);
        when(userConnectorMock.saveUser(saveUserDto))
                .thenReturn(userId);
        when(partyRegistryProxyConnector.getInstitutionsByUserFiscalCode(fiscalCode))
                .thenReturn(institutionPnPGInfo);
        //when
        InstitutionPnPGInfo result = pnPGInstitutionService.getInstitutionsByUser(user);
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
    }*/

}