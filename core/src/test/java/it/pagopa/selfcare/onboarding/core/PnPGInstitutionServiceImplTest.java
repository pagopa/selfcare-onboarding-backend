package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.onboarding.connector.api.MsCoreConnector;
import it.pagopa.selfcare.onboarding.connector.api.PartyRegistryProxyConnector;
import it.pagopa.selfcare.onboarding.connector.api.ProductsConnector;
import it.pagopa.selfcare.onboarding.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.onboarding.connector.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.onboarding.connector.model.BusinessPnPG;
import it.pagopa.selfcare.onboarding.connector.model.PnPGInstitutionLegalAddressData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionPnPGInfo;
import it.pagopa.selfcare.onboarding.connector.model.institutions.PnPGMatchInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.CreatePnPGInstitutionData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionType;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PnPGOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.onboarding.connector.model.product.Product;
import it.pagopa.selfcare.onboarding.connector.model.product.ProductRoleInfo;
import it.pagopa.selfcare.onboarding.connector.model.user.*;
import it.pagopa.selfcare.onboarding.core.exception.UpdateNotAllowedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.onboarding.connector.model.user.User.Fields.*;
import static it.pagopa.selfcare.onboarding.core.InstitutionServiceImpl.MORE_THAN_ONE_PRODUCT_ROLE_AVAILABLE;
import static it.pagopa.selfcare.onboarding.core.InstitutionServiceImpl.REQUIRED_ONBOARDING_DATA_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;
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

    @Mock
    private ProductsConnector productsConnectorMock;

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

    @Test
    void onboarding_institutionExists() throws Exception {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        PnPGOnboardingData onboardingData = mockInstance(new PnPGOnboardingData(), "setUsers", "setInstitutionType");
        onboardingData.setUsers(List.of(userInfo1, userInfo2));
        onboardingData.setInstitutionType(InstitutionType.PG);
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
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(productMock);
        when(userConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        // when
        pnPGInstitutionService.onboarding(onboardingData);
        // then
        verify(msCoreConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
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
        verifyNoMoreInteractions(msCoreConnectorMock, userConnectorMock, productsConnectorMock);
    }

    @Test
    void onboarding_nullOnboardingData() {
        // given
        PnPGOnboardingData onboardingData = null;
        // when
        Executable executable = () -> pnPGInstitutionService.onboarding(onboardingData);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_ONBOARDING_DATA_MESSAGE, e.getMessage());
        verifyNoInteractions(msCoreConnectorMock, productsConnectorMock, userConnectorMock);
    }

    @Test
    void onboarding_nullRoleMapping() {
        // given
        PnPGOnboardingData onboardingData = mockInstance(new PnPGOnboardingData(), "setInstitutionType");
        onboardingData.setInstitutionType(InstitutionType.PG);
        Product product = mockInstance(new Product(), "setId", "setParentId");
        product.setId(onboardingData.getProductId());
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(product);
        // when
        Executable executable = () -> pnPGInstitutionService.onboarding(onboardingData);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("Role mappings is required", e.getMessage());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verifyNoMoreInteractions(productsConnectorMock);
        verifyNoInteractions(msCoreConnectorMock, userConnectorMock);
    }

    @Test
    void onboarding_MoreThanOneProductRoles() {
        // given
        User userInfo = mockInstance(new User(), "setRole");
        userInfo.setRole(PartyRole.DELEGATE);
        PnPGOnboardingData onboardingData = mockInstance(new PnPGOnboardingData(), "setInstitutionType");
        onboardingData.setInstitutionType(InstitutionType.PG);
        onboardingData.setUsers(List.of(userInfo));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId", "setId");
        productMock.setId(onboardingData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        productRoleInfo1.setRoles(List.of(mockInstance(new ProductRoleInfo.ProductRole(), 1)));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        productRoleInfo2.setRoles(List.of(mockInstance(new ProductRoleInfo.ProductRole(), 1),
                mockInstance(new ProductRoleInfo.ProductRole(), 2)));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(productMock);
        // when
        Executable executable = () -> pnPGInstitutionService.onboarding(onboardingData);
        // then
        IllegalStateException e = assertThrows(IllegalStateException.class, executable);
        assertEquals(String.format(MORE_THAN_ONE_PRODUCT_ROLE_AVAILABLE, userInfo.getRole()), e.getMessage());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verifyNoMoreInteractions(productsConnectorMock);
        verifyNoInteractions(msCoreConnectorMock, userConnectorMock);
    }

    @Test
    void onboarding_createInstitution_PG() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        PnPGOnboardingData onboardingData = mockInstance(new PnPGOnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.PG);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));
        CreatePnPGInstitutionData createOnboardingData = mockInstance(new CreatePnPGInstitutionData(), "setTaxId", "setDescription");
        createOnboardingData.setTaxId("setInstitutionExternalId");
        createOnboardingData.setDescription("setBusinessName");
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
                .thenThrow(ResourceNotFoundException.class);
        when(msCoreConnectorMock.createPGInstitutionUsingExternalId(any()))
                .thenReturn(institution);
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(productMock);
        when(userConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        // when
        pnPGInstitutionService.onboarding(onboardingData);
        // then
        verify(msCoreConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verify(msCoreConnectorMock, times(1))
                .createPGInstitutionUsingExternalId(createOnboardingData);
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
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
        verifyNoMoreInteractions(productsConnectorMock, msCoreConnectorMock, userConnectorMock);
    }

    @Test
    void onboarding_userDataNotMutable() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        PnPGOnboardingData onboardingData = mockInstance(new PnPGOnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.PG);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));
        CreatePnPGInstitutionData createOnboardingData = mockInstance(new CreatePnPGInstitutionData(), "setTaxId", "setDescription");
        createOnboardingData.setTaxId("setInstitutionExternalId");
        createOnboardingData.setDescription("setBusinessName");
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
                .thenThrow(ResourceNotFoundException.class);
        when(msCoreConnectorMock.createPGInstitutionUsingExternalId(any()))
                .thenReturn(institution);
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(productMock);

        when(userConnectorMock.search(any(), any()))
                .thenAnswer(invocation -> {
                    final String taxCode = invocation.getArgument(0, String.class);
                    if (userInfo1.getTaxCode().equals(taxCode)) {
                        return Optional.empty();
                    } else {
                        final it.pagopa.selfcare.onboarding.connector.model.user.User user = new it.pagopa.selfcare.onboarding.connector.model.user.User();
                        final CertifiedField<String> familyName = new CertifiedField<>();
                        familyName.setCertification(Certification.NONE);
                        familyName.setValue("setSurname2");
                        user.setFamilyName(familyName);
                        final CertifiedField<String> email = new CertifiedField<>();
                        email.setCertification(Certification.SPID);
                        email.setValue("different value");
                        final WorkContact workContact = new WorkContact();
                        workContact.setEmail(email);
                        user.setWorkContacts(Map.of(institution.getId(), workContact));
                        return Optional.of(user);
                    }
                });
        when(userConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        // when
        final Executable executable = () -> pnPGInstitutionService.onboarding(onboardingData);
        // then
        assertThrows(UpdateNotAllowedException.class, executable);
        verify(msCoreConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verify(msCoreConnectorMock, times(1))
                .createPGInstitutionUsingExternalId(createOnboardingData);
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(userConnectorMock, times(1))
                .saveUser(any());
        onboardingData.getUsers().forEach(user ->
                verify(userConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verifyNoMoreInteractions(productsConnectorMock, msCoreConnectorMock, userConnectorMock);
    }

    @Test
    void onboarding_userDataMutable() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);

        PnPGOnboardingData onboardingData = mockInstance(new PnPGOnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.PG);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));

        CreatePnPGInstitutionData createOnboardingData = mockInstance(new CreatePnPGInstitutionData(), "setTaxId", "setDescription");
        createOnboardingData.setTaxId("setInstitutionExternalId");
        createOnboardingData.setDescription("setBusinessName");

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
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(productMock);
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(msCoreConnectorMock.getInstitutionByExternalId(anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(msCoreConnectorMock.createPGInstitutionUsingExternalId(any()))
                .thenReturn(institution);

        when(userConnectorMock.search(any(), any()))
                .thenAnswer(invocation -> {
                    final String taxCode = invocation.getArgument(0, String.class);
                    if (userInfo1.getTaxCode().equals(taxCode)) {
                        return Optional.empty();
                    } else {
                        final it.pagopa.selfcare.onboarding.connector.model.user.User user = new it.pagopa.selfcare.onboarding.connector.model.user.User();
                        final CertifiedField<String> name = new CertifiedField<>();
                        name.setCertification(Certification.NONE);
                        name.setValue("setName2");
                        user.setName(name);
                        final CertifiedField<String> familyName = new CertifiedField<>();
                        familyName.setCertification(Certification.NONE);
                        familyName.setValue("setSurname1");
                        user.setFamilyName(familyName);
                        final CertifiedField<String> email = new CertifiedField<>();
                        email.setCertification(Certification.NONE);
                        email.setValue("setEmail1");
                        final WorkContact workContact = new WorkContact();
                        workContact.setEmail(email);
                        user.setWorkContacts(Map.of(institution.getId(), workContact));
                        user.setId(UUID.randomUUID().toString());
                        return Optional.of(user);
                    }
                });
        when(userConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        //when
        final Executable executable = () -> pnPGInstitutionService.onboarding(onboardingData);
        //then
        assertDoesNotThrow(executable);
        verify(msCoreConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verify(msCoreConnectorMock, times(1))
                .createPGInstitutionUsingExternalId(createOnboardingData);
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        verify(userConnectorMock, times(1))
                .saveUser(saveUserCaptor.capture());
        onboardingData.getUsers().forEach(user ->
                verify(userConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userConnectorMock, times(1))
                .updateUser(any(), any());
        verify(msCoreConnectorMock, times(1))
                .onboardingPGOrganization(any());
        verifyNoMoreInteractions(productsConnectorMock);
    }

}