package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.connector.api.PartyConnector;
import it.pagopa.selfcare.onboarding.connector.api.ProductsConnector;
import it.pagopa.selfcare.onboarding.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.onboarding.connector.exceptions.ManagerNotFoundException;
import it.pagopa.selfcare.onboarding.connector.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipInfo;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipState;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipsResponse;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Attribute;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.*;
import it.pagopa.selfcare.onboarding.connector.model.product.Product;
import it.pagopa.selfcare.onboarding.connector.model.product.ProductRoleInfo;
import it.pagopa.selfcare.onboarding.connector.model.user.*;
import it.pagopa.selfcare.onboarding.core.exception.UpdateNotAllowedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.onboarding.connector.model.user.User.Fields.*;
import static it.pagopa.selfcare.onboarding.core.InstitutionServiceImpl.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstitutionServiceImplTest {

    @InjectMocks
    private InstitutionServiceImpl institutionService;

    @Mock
    private PartyConnector partyConnectorMock;

    @Mock
    private ProductsConnector productsConnectorMock;

    @Mock
    private UserRegistryConnector userConnectorMock;

    @Captor
    private ArgumentCaptor<OnboardingData> onboardingDataCaptor;

    @Captor
    private ArgumentCaptor<UserInfo.UserInfoFilter> userInfoFilterCaptor;

    @BeforeEach
    void beforeEach() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void onboarding_nullOnboardingData() {
        // given
        OnboardingData onboardingData = null;
        // when
        Executable executable = () -> institutionService.onboarding(onboardingData);
        // then
        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals(REQUIRED_ONBOARDING_DATA_MESSAGE, e.getMessage());
        verifyNoInteractions(partyConnectorMock, productsConnectorMock, userConnectorMock);
    }


    @Test
    void onboarding_nullRoleMapping() {
        // given
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Product product = mockInstance(new Product(), "setParentId");
        when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(product);
        // when
        Executable executable = () -> institutionService.onboarding(onboardingData);
        // then
        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("Role mappings is required", e.getMessage());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId());
        verifyNoMoreInteractions(productsConnectorMock);
        verifyNoInteractions(partyConnectorMock, userConnectorMock);
    }

    @Test
    void onboarding_nullBillingData() {
        //given
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setBilling");
        //when
        Executable executable = () -> institutionService.onboarding(onboardingData);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_BILLING_DATA_MESSAGE, e.getMessage());
        verifyNoInteractions(productsConnectorMock, partyConnectorMock, userConnectorMock);
    }

    @Test
    void onboarding_nullOrganizationType() {
        //given
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType");
        Billing billing = mockInstance(new Billing());
        onboardingData.setBilling(billing);
        //when
        Executable executable = () -> institutionService.onboarding(onboardingData);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_TYPE_MESSAGE, e.getMessage());
        verifyNoInteractions(productsConnectorMock, partyConnectorMock, userConnectorMock);
    }

    @Test
    void onboarding_nullProductRoles() {
        // given
        User userInfo = mockInstance(new User(), "setRole");
        userInfo.setRole(PartyRole.MANAGER);
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Billing billing = mockInstance(new Billing());
        onboardingData.setUsers(List.of(userInfo));
        onboardingData.setBilling(billing);
        Product product = mockInstance(new Product(), "setParentId");
        product.setRoleMappings(new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, null);
        }});
        when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(product);
        // when
        Executable executable = () -> institutionService.onboarding(onboardingData);
        // then
        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals(String.format(ATLEAST_ONE_PRODUCT_ROLE_REQUIRED, userInfo.getRole()), e.getMessage());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId());
        verifyNoMoreInteractions(productsConnectorMock);
        verifyNoInteractions(partyConnectorMock, userConnectorMock);
    }


    @Test
    void onboarding_emptyProductRoles() {
        // given
        User userInfo = mockInstance(new User(), "setRole");
        userInfo.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Billing billing = mockInstance(new Billing());
        onboardingData.setBilling(billing);
        onboardingData.setUsers(List.of(userInfo));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId");
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        productRoleInfo1.setRoles(List.of(mockInstance(new ProductRoleInfo.ProductRole(), 1)));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        productRoleInfo2.setRoles(List.of());
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(productMock);
        // when
        Executable executable = () -> institutionService.onboarding(onboardingData);
        // then
        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals(String.format(ATLEAST_ONE_PRODUCT_ROLE_REQUIRED, userInfo.getRole()), e.getMessage());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId());
        verifyNoMoreInteractions(productsConnectorMock);
        verifyNoInteractions(partyConnectorMock, userConnectorMock);
    }

    @Test
    void onboarding_MoreThanOneProductRoles() {
        // given
        User userInfo = mockInstance(new User(), "setRole");
        userInfo.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Billing billing = mockInstance(new Billing());
        onboardingData.setBilling(billing);
        onboardingData.setUsers(List.of(userInfo));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId");
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
        when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(productMock);
        // when
        Executable executable = () -> institutionService.onboarding(onboardingData);
        // then
        IllegalStateException e = Assertions.assertThrows(IllegalStateException.class, executable);
        Assertions.assertEquals(String.format(MORE_THAN_ONE_PRODUCT_ROLE_AVAILABLE, userInfo.getRole()), e.getMessage());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId());
        verifyNoMoreInteractions(productsConnectorMock);
        verifyNoInteractions(partyConnectorMock, userConnectorMock);
    }


    @Test
    void onboarding_institutionExists() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Billing billing = mockInstance(new Billing());
        onboardingData.setBilling(billing);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId");
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
        when(partyConnectorMock.getInstitutionByExternalId(Mockito.anyString()))
                .thenReturn(institution);
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(productMock);

        when(userConnectorMock.saveUser(Mockito.any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });

        // when
        institutionService.onboarding(onboardingData);
        // then
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId());
        verify(partyConnectorMock, times(1))
                .onboardingOrganization(onboardingDataCaptor.capture());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        onboardingData.getUsers().forEach(user ->
                verify(userConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userConnectorMock, times(2))
                .saveUser(saveUserCaptor.capture());
        List<SaveUserDto> savedUsers = saveUserCaptor.getAllValues();
        savedUsers.forEach(saveUserDto -> assertTrue(saveUserDto.getWorkContacts().containsKey(institution.getId())));
        OnboardingData captured = onboardingDataCaptor.getValue();
        Assertions.assertNotNull(captured.getUsers());
        Assertions.assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            Assertions.assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userConnectorMock);
    }

    @Test
    void onboarding_createInstitution() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Billing billing = mockInstance(new Billing());
        onboardingData.setBilling(billing);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId");
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
        when(partyConnectorMock.getInstitutionByExternalId(Mockito.anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionUsingExternalId(Mockito.anyString()))
                .thenReturn(institution);
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(productMock);

        when(userConnectorMock.saveUser(Mockito.any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });

        // when
        institutionService.onboarding(onboardingData);
        // then
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .createInstitutionUsingExternalId(onboardingData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId());
        verify(partyConnectorMock, times(1))
                .onboardingOrganization(onboardingDataCaptor.capture());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        onboardingData.getUsers().forEach(user ->
                verify(userConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userConnectorMock, times(2))
                .saveUser(saveUserCaptor.capture());
        List<SaveUserDto> savedUsers = saveUserCaptor.getAllValues();
        savedUsers.forEach(saveUserDto -> assertTrue(saveUserDto.getWorkContacts().containsKey(institution.getId())));
        OnboardingData captured = onboardingDataCaptor.getValue();
        Assertions.assertNotNull(captured.getUsers());
        Assertions.assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            Assertions.assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userConnectorMock);
    }


    @Test
    void onboarding_userDataNotMutable() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Billing billing = mockInstance(new Billing());
        onboardingData.setBilling(billing);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));
        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId");
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
        when(partyConnectorMock.getInstitutionByExternalId(Mockito.anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionUsingExternalId(Mockito.anyString()))
                .thenReturn(institution);
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(productMock);

        when(userConnectorMock.search(Mockito.any(), Mockito.any()))
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
        when(userConnectorMock.saveUser(Mockito.any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });

        // when
        final Executable executable = () -> institutionService.onboarding(onboardingData);
        // then
        assertThrows(UpdateNotAllowedException.class, executable);
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .createInstitutionUsingExternalId(onboardingData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        verify(userConnectorMock, times(1))
                .saveUser(Mockito.any());
        onboardingData.getUsers().forEach(user ->
                verify(userConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userConnectorMock);
    }

    @Test
    void onboarding_userDataMutable() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);

        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Billing billing = mockInstance(new Billing());
        onboardingData.setBilling(billing);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));

        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId");
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
        when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(productMock);
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.getInstitutionByExternalId(Mockito.anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionUsingExternalId(Mockito.anyString()))
                .thenReturn(institution);

        when(userConnectorMock.search(Mockito.any(), Mockito.any()))
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
        when(userConnectorMock.saveUser(Mockito.any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        //when
        final Executable executable = () -> institutionService.onboarding(onboardingData);
        //then
        assertDoesNotThrow(executable);
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .createInstitutionUsingExternalId(onboardingData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        verify(userConnectorMock, times(1))
                .saveUser(saveUserCaptor.capture());
        onboardingData.getUsers().forEach(user ->
                verify(userConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userConnectorMock, times(1))
                .updateUser(any(), any());
        verify(partyConnectorMock, times(1))
                .onboardingOrganization(any());
        verifyNoMoreInteractions(productsConnectorMock);
    }

    @Test
    void onboarding_userDataMutable1() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);

        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Billing billing = mockInstance(new Billing());
        onboardingData.setBilling(billing);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));

        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId");
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
        when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(productMock);
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.getInstitutionByExternalId(Mockito.anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionUsingExternalId(Mockito.anyString()))
                .thenReturn(institution);

        when(userConnectorMock.search(Mockito.any(), Mockito.any()))
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
                        familyName.setCertification(Certification.SPID);
                        familyName.setValue(userInfo2.getSurname().toUpperCase());
                        user.setFamilyName(familyName);
                        user.setId(UUID.randomUUID().toString());
                        return Optional.of(user);
                    }
                });
        when(userConnectorMock.saveUser(Mockito.any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        //when
        final Executable executable = () -> institutionService.onboarding(onboardingData);
        //then
        assertDoesNotThrow(executable);
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .createInstitutionUsingExternalId(onboardingData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        verify(userConnectorMock, times(1))
                .saveUser(saveUserCaptor.capture());
        onboardingData.getUsers().forEach(user ->
                verify(userConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userConnectorMock, times(1))
                .updateUser(any(), any());
        verify(partyConnectorMock, times(1))
                .onboardingOrganization(any());
        verifyNoMoreInteractions(productsConnectorMock);
    }

    @Test
    void onboarding_userDataMutable3() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);

        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Billing billing = mockInstance(new Billing());
        onboardingData.setBilling(billing);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));

        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId");
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
        when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(productMock);
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.getInstitutionByExternalId(Mockito.anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionUsingExternalId(Mockito.anyString()))
                .thenReturn(institution);

        when(userConnectorMock.search(Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> {
                    final String taxCode = invocation.getArgument(0, String.class);
                    if (userInfo1.getTaxCode().equals(taxCode)) {
                        return Optional.empty();
                    } else {
                        final it.pagopa.selfcare.onboarding.connector.model.user.User user = new it.pagopa.selfcare.onboarding.connector.model.user.User();
                        final CertifiedField<String> name = new CertifiedField<>();
                        name.setCertification(Certification.NONE);
                        name.setValue(userInfo2.getName().toUpperCase());
                        user.setName(name);
                        final CertifiedField<String> familyName = new CertifiedField<>();
                        familyName.setCertification(Certification.SPID);
                        familyName.setValue("setSurname2");
                        user.setFamilyName(familyName);
                        final CertifiedField<String> email = new CertifiedField<>();
                        email.setCertification(Certification.NONE);
                        email.setValue("setEmail2");
                        final WorkContact workContact = new WorkContact();
                        workContact.setEmail(email);
                        user.setWorkContacts(Map.of(institution.getId(), workContact));
                        user.setId(UUID.randomUUID().toString());
                        user.setId(UUID.randomUUID().toString());
                        return Optional.of(user);
                    }
                });
        when(userConnectorMock.saveUser(Mockito.any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        //when
        final Executable executable = () -> institutionService.onboarding(onboardingData);
        //then
        assertDoesNotThrow(executable);
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .createInstitutionUsingExternalId(onboardingData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        verify(userConnectorMock, times(1))
                .saveUser(saveUserCaptor.capture());
        onboardingData.getUsers().forEach(user ->
                verify(userConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(partyConnectorMock, times(1))
                .onboardingOrganization(any());
        verifyNoMoreInteractions(productsConnectorMock);
    }

    @Test
    void onboarding_userDataMutable2() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);

        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Billing billing = mockInstance(new Billing());
        onboardingData.setBilling(billing);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));

        Product productMock = mockInstance(new Product(), "setRoleMappings", "setParentId");
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
        when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(productMock);
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.getInstitutionByExternalId(Mockito.anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionUsingExternalId(Mockito.anyString()))
                .thenReturn(institution);

        when(userConnectorMock.search(Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> {
                    final String taxCode = invocation.getArgument(0, String.class);
                    if (userInfo1.getTaxCode().equals(taxCode)) {
                        return Optional.empty();
                    } else {
                        final it.pagopa.selfcare.onboarding.connector.model.user.User user = new it.pagopa.selfcare.onboarding.connector.model.user.User();
                        final CertifiedField<String> name = new CertifiedField<>();
                        name.setCertification(Certification.NONE);
                        name.setValue("setName3");
                        user.setName(name);
                        final CertifiedField<String> familyName = new CertifiedField<>();
                        familyName.setCertification(Certification.SPID);
                        familyName.setValue("setSurname2");
                        user.setFamilyName(familyName);
                        final CertifiedField<String> email = new CertifiedField<>();
                        email.setCertification(Certification.NONE);
                        email.setValue("setEmail1");
                        final WorkContact workContact = new WorkContact();
                        workContact.setEmail(email);
                        user.setWorkContacts(Map.of("differentKey", workContact));
                        user.setId(UUID.randomUUID().toString());
                        return Optional.of(user);
                    }
                });
        when(userConnectorMock.saveUser(Mockito.any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        //when
        final Executable executable = () -> institutionService.onboarding(onboardingData);
        //then
        assertDoesNotThrow(executable);
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verify(partyConnectorMock, times(1))
                .createInstitutionUsingExternalId(onboardingData.getInstitutionExternalId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId());
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        verify(userConnectorMock, times(1))
                .saveUser(saveUserCaptor.capture());
        onboardingData.getUsers().forEach(user ->
                verify(userConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        verify(userConnectorMock, times(1))
                .updateUser(any(), any());
        verify(partyConnectorMock, times(1))
                .onboardingOrganization(any());
        verifyNoMoreInteractions(productsConnectorMock);
    }

    @Test
    void onboarding_noRelationshipForSubProduct() {
        //given
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Billing billing = mockInstance(new Billing());
        onboardingData.setBilling(billing);
        Product baseProductMock = mockInstance(new Product(), 1, "setParentId");
        Product subProductMock = mockInstance(new Product(), 2, "setParentId", "setRoleMappings");
        subProductMock.setParentId(baseProductMock.getId());
        when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(subProductMock);
        when(productsConnectorMock.getProduct(subProductMock.getParentId()))
                .thenReturn(baseProductMock);
        //when
        Executable executable = () -> institutionService.onboarding(onboardingData);
        //then
        ManagerNotFoundException e = Assertions.assertThrows(ManagerNotFoundException.class, executable);
        assertEquals("Unable to retrieve the manager related to institution external id = " + onboardingData.getInstitutionExternalId() + " and base product " + subProductMock.getParentId(), e.getMessage());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId());
        verify(productsConnectorMock, times(1))
                .getProduct(subProductMock.getParentId());
        verify(partyConnectorMock, times(1))
                .getUserInstitutionRelationships(eq(onboardingData.getInstitutionExternalId()), userInfoFilterCaptor.capture());
        assertEquals(Optional.of(EnumSet.of(PartyRole.MANAGER)), userInfoFilterCaptor.getValue().getRole());
        assertEquals(Optional.of(EnumSet.of(RelationshipState.ACTIVE)), userInfoFilterCaptor.getValue().getAllowedStates());
        assertEquals(Optional.of(baseProductMock.getId()), userInfoFilterCaptor.getValue().getProductId());
        verifyNoMoreInteractions(partyConnectorMock, productsConnectorMock);
        verifyNoInteractions(userConnectorMock);
    }

    @Test
    void onboardingSubProduct_foundMoreThanOneManager() {
        //given
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Billing billing = mockInstance(new Billing());
        onboardingData.setBilling(billing);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));
        Product baseProductMock = mockInstance(new Product(), 1, "setParentId", "setRoleMappings");
        Product subProductMock = mockInstance(new Product(), 2, "setParentId", "setRoleMappings");
        subProductMock.setParentId(baseProductMock.getId());
        when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(subProductMock);
        when(productsConnectorMock.getProduct(subProductMock.getParentId()))
                .thenReturn(baseProductMock);
        RelationshipInfo relationshipInfoMock = mockInstance(new RelationshipInfo());
        relationshipInfoMock.setRole(PartyRole.MANAGER);
        RelationshipsResponse relationshipsResponse = new RelationshipsResponse();
        relationshipsResponse.add(relationshipInfoMock);
        relationshipsResponse.add(relationshipInfoMock);
        when(partyConnectorMock.getUserInstitutionRelationships(any(), any()))
                .thenReturn(relationshipsResponse);
        //when
        Executable executable = () -> institutionService.onboarding(onboardingData);
        //then
        ManagerNotFoundException e = assertThrows(ManagerNotFoundException.class, executable);
        assertEquals("Unable to retrieve the manager related to institution external id = " + onboardingData.getInstitutionExternalId() + " and base product " + baseProductMock.getId(), e.getMessage());
        verify(partyConnectorMock, times(1))
                .getUserInstitutionRelationships(eq(onboardingData.getInstitutionExternalId()), userInfoFilterCaptor.capture());
        assertEquals(Optional.of(EnumSet.of(PartyRole.MANAGER)), userInfoFilterCaptor.getValue().getRole());
        assertEquals(Optional.of(EnumSet.of(RelationshipState.ACTIVE)), userInfoFilterCaptor.getValue().getAllowedStates());
        assertEquals(Optional.of(baseProductMock.getId()), userInfoFilterCaptor.getValue().getProductId());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId());
        verifyNoMoreInteractions(partyConnectorMock, productsConnectorMock);
        verifyNoInteractions(userConnectorMock);
    }


    @Test
    void onboardingSubProduct() {
        // given
        String productRole = "role";
        User userInfo1 = mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Billing billing = mockInstance(new Billing());
        onboardingData.setBilling(billing);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));
        Product baseProductMock = mockInstance(new Product(), "setRoleMappings", "setParentId");
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
        baseProductMock.setRoleMappings(roleMappings);
        Product subProductMock = mockInstance(new Product(), "setId");
        subProductMock.setId(onboardingData.getProductId());
        subProductMock.setParentId(baseProductMock.getId());
        when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(subProductMock);
        when(productsConnectorMock.getProduct(subProductMock.getParentId()))
                .thenReturn(baseProductMock);
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        RelationshipInfo relationshipInfoMock = mockInstance(new RelationshipInfo());
        relationshipInfoMock.setTo(institution.getId());
        RelationshipsResponse relationshipsResponse = new RelationshipsResponse();
        relationshipsResponse.add(relationshipInfoMock);
        when(partyConnectorMock.getUserInstitutionRelationships(any(), any()))
                .thenReturn(relationshipsResponse);
        when(userConnectorMock.getUserByInternalId(any(), any()))
                .thenAnswer(invocation -> {
                    final it.pagopa.selfcare.onboarding.connector.model.user.User user =
                            mockInstance(new it.pagopa.selfcare.onboarding.connector.model.user.User());
                    user.setId(invocation.getArgument(0, String.class));
                    user.setFiscalCode(userInfo1.getTaxCode());
                    WorkContact contact = mockInstance(new WorkContact());
                    Map<String, WorkContact> workContacts = new HashMap<>();
                    workContacts.put(institution.getId(), contact);
                    user.setWorkContacts(workContacts);
                    return user;
                });
        when(partyConnectorMock.getInstitutionByExternalId(Mockito.anyString()))
                .thenReturn(institution);
        when(userConnectorMock.saveUser(Mockito.any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        // when
        institutionService.onboarding(onboardingData);
        // then
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId());
        verify(partyConnectorMock, times(1))
                .getUserInstitutionRelationships(eq(onboardingData.getInstitutionExternalId()), userInfoFilterCaptor.capture());
        assertEquals(Optional.of(EnumSet.of(PartyRole.MANAGER)), userInfoFilterCaptor.getValue().getRole());
        assertEquals(Optional.of(EnumSet.of(RelationshipState.ACTIVE)), userInfoFilterCaptor.getValue().getAllowedStates());
        assertEquals(Optional.of(subProductMock.getParentId()), userInfoFilterCaptor.getValue().getProductId());
        verify(productsConnectorMock, times(1))
                .getProduct(subProductMock.getParentId());
        verify(partyConnectorMock, times(1))
                .onboardingOrganization(onboardingDataCaptor.capture());
        OnboardingData captured = onboardingDataCaptor.getValue();
        Assertions.assertNotNull(captured.getUsers());
        Assertions.assertEquals(1, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            Assertions.assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
        onboardingData.getUsers().forEach(user ->
                verify(userConnectorMock, times(1))
                        .search(user.getTaxCode(), EnumSet.of(name, familyName, workContacts)));
        ArgumentCaptor<SaveUserDto> saveUserCaptor = ArgumentCaptor.forClass(SaveUserDto.class);
        verify(userConnectorMock, times(1))
                .saveUser(saveUserCaptor.capture());
        List<SaveUserDto> savedUsers = saveUserCaptor.getAllValues();
        savedUsers.forEach(saveUserDto -> assertTrue(saveUserDto.getWorkContacts().containsKey(institution.getId())));

        verify(userConnectorMock, times(1))
                .getUserByInternalId(relationshipInfoMock.getFrom(), EnumSet.of(fiscalCode, name, familyName, workContacts));
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(onboardingData.getInstitutionExternalId());
        verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock, userConnectorMock);
    }


    @Test
    void getInstitutions() {
        //given
        InstitutionInfo expectedInstitutionInfo = new InstitutionInfo();
        when(partyConnectorMock.getOnBoardedInstitutions())
                .thenReturn(List.of(expectedInstitutionInfo));
        // when
        Collection<InstitutionInfo> institutions = institutionService.getInstitutions();
        // then
        assertNotNull(institutions);
        assertEquals(1, institutions.size());
        assertSame(expectedInstitutionInfo, institutions.iterator().next());
        verify(partyConnectorMock, times(1))
                .getOnBoardedInstitutions();
        verifyNoMoreInteractions(partyConnectorMock);
        verifyNoInteractions(productsConnectorMock, userConnectorMock);
    }

    @Test
    void getInstitutions_emptyResult() {
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
    }

    @Test
    void getInstitutionOnboardingData_nullInstitutionId() {
        //given
        String institutionId = null;
        String productId = "productId";
        //when
        Executable executable = () -> institutionService.getInstitutionOnboardingData(institutionId, productId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_ID_MESSAGE, e.getMessage());
        verifyNoInteractions(partyConnectorMock, productsConnectorMock, userConnectorMock);
    }

    @Test
    void getInstitutionOnboardingData_ManagerNotFound() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        //when
        Executable executable = () -> institutionService.getInstitutionOnboardingData(institutionId, productId);
        //then
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, executable);
        assertEquals(String.format("No Manager found for given institution: %s", institutionId), e.getMessage());

        verify(partyConnectorMock, times(1))
                .getInstitutionManager(institutionId, productId);
        verify(partyConnectorMock, times(0))
                .getOnboardedInstitution(institutionId);
        verifyNoMoreInteractions(partyConnectorMock);
        verifyNoInteractions(productsConnectorMock, userConnectorMock);
    }

    @Test
    void getInstitutionOnboardingData_institutionNotFound() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        UserInfo userInfoManager = mockInstance(new UserInfo());
        userInfoManager.setRole(PartyRole.MANAGER);
        when(partyConnectorMock.getInstitutionManager(any(), any()))
                .thenReturn(userInfoManager);
        final it.pagopa.selfcare.onboarding.connector.model.user.User userManagerMock =
                new it.pagopa.selfcare.onboarding.connector.model.user.User();
        when(userConnectorMock.getUserByInternalId(any(), any()))
                .thenReturn(userManagerMock);
        //when
        Executable executable = () -> institutionService.getInstitutionOnboardingData(institutionId, productId);
        //then
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, executable);
        assertEquals(String.format("Institution %s not found", institutionId), e.getMessage());
        verify(partyConnectorMock, times(1))
                .getInstitutionManager(institutionId, productId);
        verify(partyConnectorMock, times(1))
                .getOnboardedInstitution(institutionId);
        verify(userConnectorMock, times(1))
                .getUserByInternalId(userInfoManager.getId(), EnumSet.of(name, familyName, workContacts, fiscalCode));
        verifyNoMoreInteractions(partyConnectorMock, userConnectorMock);
        verifyNoInteractions(productsConnectorMock);
    }


    @Test
    void getInstitutionOnboardingData() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        String loggedUser = "loggedUser";
        UserInfo userInfoMock = mockInstance(new UserInfo(), "setId", "setRole");
        userInfoMock.setId(loggedUser);
        UserInfo userInfoManager = mockInstance(new UserInfo());
        userInfoManager.setRole(PartyRole.MANAGER);
        when(partyConnectorMock.getInstitutionManager(any(), any()))
                .thenReturn(userInfoManager);
        final it.pagopa.selfcare.onboarding.connector.model.user.User userManagerMock =
                new it.pagopa.selfcare.onboarding.connector.model.user.User();
        when(userConnectorMock.getUserByInternalId(any(), any()))
                .thenReturn(userManagerMock);
        InstitutionInfo institutionInfoMock = mockInstance(new InstitutionInfo());
        Billing billingMock = mockInstance(new Billing());
        institutionInfoMock.setBilling(billingMock);
        when(partyConnectorMock.getOnboardedInstitution(Mockito.anyString()))
                .thenReturn(institutionInfoMock);
        //when
        InstitutionOnboardingData institutionOnboardingData = institutionService.getInstitutionOnboardingData(institutionId, productId);
        //then
        assertNotNull(institutionOnboardingData);
        assertNotNull(institutionOnboardingData.getManager());
        assertEquals(userInfoManager, institutionOnboardingData.getManager());
        assertNotNull(institutionOnboardingData.getInstitution());
        assertEquals(institutionInfoMock, institutionOnboardingData.getInstitution());

        verify(partyConnectorMock, times(1))
                .getInstitutionManager(institutionId, productId);
        verify(partyConnectorMock, times(1))
                .getOnboardedInstitution(institutionId);
        verify(userConnectorMock, times(1))
                .getUserByInternalId(userInfoManager.getId(), EnumSet.of(name, familyName, workContacts, fiscalCode));
        verifyNoMoreInteractions(partyConnectorMock, userConnectorMock);
        verifyNoInteractions(productsConnectorMock);
    }

    @Test
    void getInstitutionByExternalId_nullInstitutionId() {
        //given
        String institutionId = null;
        //when
        Executable executable = () -> institutionService.getInstitutionByExternalId(institutionId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_ID_MESSAGE, e.getMessage());
        verifyNoInteractions(partyConnectorMock, productsConnectorMock, userConnectorMock);
    }

    @Test
    void getInstitutionByExternalId() {
        //given
        String institutionId = "institutionId";
        Institution institutionMock = mockInstance(new Institution());
        Attribute attribute = mockInstance(new Attribute());
        institutionMock.setAttributes(List.of(attribute));
        when(partyConnectorMock.getInstitutionByExternalId(Mockito.anyString()))
                .thenReturn(institutionMock);
        //when
        Institution result = institutionService.getInstitutionByExternalId(institutionId);
        //then
        assertNotNull(result);
        assertEquals(institutionMock.getExternalId(), result.getExternalId());
        assertEquals(institutionMock.getId(), result.getId());
        assertEquals(institutionMock.getOrigin(), result.getOrigin());
        assertEquals(institutionMock.getInstitutionType(), result.getInstitutionType());
        assertEquals(institutionMock.getDescription(), result.getDescription());
        assertEquals(institutionMock.getTaxCode(), result.getTaxCode());
        assertEquals(institutionMock.getZipCode(), result.getZipCode());
        assertEquals(institutionMock.getAddress(), result.getAddress());
        assertEquals(institutionMock.getAttributes(), result.getAttributes());
        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(institutionId);
        verifyNoMoreInteractions(partyConnectorMock);
        verifyNoInteractions(productsConnectorMock, userConnectorMock);
    }

}