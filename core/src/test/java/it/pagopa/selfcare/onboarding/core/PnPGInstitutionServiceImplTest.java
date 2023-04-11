package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.connector.api.MsCoreConnector;
import it.pagopa.selfcare.onboarding.connector.api.PartyRegistryProxyConnector;
import it.pagopa.selfcare.onboarding.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.onboarding.connector.model.BusinessPnPG;
import it.pagopa.selfcare.onboarding.connector.model.PnPGInstitutionLegalAddressData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionPnPGInfo;
import it.pagopa.selfcare.onboarding.connector.model.institutions.PnPGMatchInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PnPGOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.onboarding.connector.model.user.SaveUserDto;
import it.pagopa.selfcare.onboarding.connector.model.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PnPGInstitutionServiceImplTest {

    @InjectMocks
    private PnPGInstitutionServiceImpl pnPGInstitutionService;

    @Mock
    private PartyRegistryProxyConnector partyRegistryProxyConnectorMock;

    @Mock
    private MsCoreConnector msCoreConnectorMock;

    @Mock
    private UserRegistryConnector userConnectorMock;

    @Captor
    private ArgumentCaptor<PnPGOnboardingData> onboardingDataCaptor;

    @BeforeEach
    void beforeEach() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getInstitutionsByUser_default() {
        //given
        String taxCode = "setTaxCode";
        SaveUserDto saveUserDto = mockInstance(new SaveUserDto(), "setFiscalCode");
        saveUserDto.setFiscalCode(taxCode);
        UserId userId = mockInstance(new UserId());
        User user = mockInstance(new User(), "setId");
        user.setId(userId.toString());
        List<BusinessPnPG> businessPnPGList = List.of(mockInstance(new BusinessPnPG()));
        InstitutionPnPGInfo institutionPnPGInfo = mockInstance(new InstitutionPnPGInfo(), "setBusinesses");
        institutionPnPGInfo.setBusinesses(businessPnPGList);
        when(partyRegistryProxyConnectorMock.getInstitutionsByUserFiscalCode(anyString()))
                .thenReturn(institutionPnPGInfo);
        //when
        InstitutionPnPGInfo result = pnPGInstitutionService.getInstitutionsByUser(user);
        //then
        assertNotNull(result);
        assertEquals(institutionPnPGInfo.getBusinesses().get(0).getBusinessName(), result.getBusinesses().get(0).getBusinessName());
        assertEquals(institutionPnPGInfo.getBusinesses().get(0).getBusinessTaxId(), result.getBusinesses().get(0).getBusinessTaxId());
        assertEquals(institutionPnPGInfo.getLegalTaxId(), result.getLegalTaxId());
        assertEquals(institutionPnPGInfo.getRequestDateTime(), result.getRequestDateTime());
        verify(partyRegistryProxyConnectorMock, times(1))
                .getInstitutionsByUserFiscalCode(taxCode);
        verifyNoMoreInteractions(partyRegistryProxyConnectorMock);
        verifyNoMoreInteractions(userConnectorMock);
    }

    @Test
    void matchInstitutionAndUser() {
        //given
        String externalId = "externalId";
        String taxCode = "setTaxCode";
        UserId userId = mockInstance(new UserId());
        User user = mockInstance(new User(), "setId");
        user.setId(userId.toString());
        PnPGMatchInfo pnPGMatchInfo = mockInstance(new PnPGMatchInfo());
        when(partyRegistryProxyConnectorMock.matchInstitutionAndUser(anyString(), anyString()))
                .thenReturn(pnPGMatchInfo);
        //when
        PnPGMatchInfo result = pnPGInstitutionService.matchInstitutionAndUser(externalId, user);
        //then
        assertNotNull(result);
        assertEquals(result.isVerificationResult(), pnPGMatchInfo.isVerificationResult());
        verify(partyRegistryProxyConnectorMock, times(1))
                .matchInstitutionAndUser(externalId, taxCode);
        verifyNoMoreInteractions(partyRegistryProxyConnectorMock);

    }

    @Test
    void getInstitutionLegalAddress() {
        //given
        String externalId = "externalId";
        PnPGInstitutionLegalAddressData data = mockInstance(new PnPGInstitutionLegalAddressData());
        when(partyRegistryProxyConnectorMock.getInstitutionLegalAddress(anyString()))
                .thenReturn(data);
        //when
        PnPGInstitutionLegalAddressData result = pnPGInstitutionService.getInstitutionLegalAddress(externalId);
        //then
        assertNotNull(result);
        assertEquals(result.getAddress(), data.getAddress());
        assertEquals(result.getZipCode(), data.getZipCode());
        verify(partyRegistryProxyConnectorMock, times(1))
                .getInstitutionLegalAddress(externalId);
        verifyNoMoreInteractions(partyRegistryProxyConnectorMock);

    }

    // fixme: test onboarding successfull
    /*@Test
    void onboarding_institutionExists() throws Exception {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        PnPGOnboardingData onboardingData = mockInstance(new PnPGOnboardingData(), "setUsers");
        onboardingData.setUsers(List.of(userInfo1, userInfo2));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(msCoreConnectorMock.getInstitutionByExternalId(anyString()))
                .thenReturn(institution);
        productMock.setRoleMappings(roleMappings);
        when(userConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        // when
        try {
            //when
            pnPGInstitutionService.onboarding(onboardingData);
        } catch (Exception e) {
            // do nothing
        }
        // then
        verify(msCoreConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verify(msCoreConnectorMock, times(1))
                .onboardingPGOrganization(onboardingDataCaptor.capture());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        onboardingData.getUsers().forEach(user ->
                verify(userConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userConnectorMock, times(2))
                .saveUser(saveUserCaptor.capture());
        List<SaveUserDto> savedUsers = saveUserCaptor.getAllValues();
        savedUsers.forEach(saveUserDto -> assertTrue(saveUserDto.getWorkContacts().containsKey(institution.getId())));
        PnPGOnboardingData captured = onboardingDataCaptor.getValue();
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
        verifyNoMoreInteractions(msCoreConnectorMock, userConnectorMock);
    }*/

}