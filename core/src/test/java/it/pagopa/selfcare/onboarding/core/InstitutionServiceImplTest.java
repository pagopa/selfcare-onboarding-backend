package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.commons.base.utils.Origin;
import it.pagopa.selfcare.onboarding.common.InstitutionType;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import it.pagopa.selfcare.onboarding.common.ProductId;
import it.pagopa.selfcare.onboarding.connector.api.*;
import it.pagopa.selfcare.onboarding.connector.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionLegalAddressData;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.RecipientCodeStatusResult;
import it.pagopa.selfcare.onboarding.connector.model.institutions.*;
import it.pagopa.selfcare.onboarding.connector.model.institutions.infocamere.BusinessInfoIC;
import it.pagopa.selfcare.onboarding.connector.model.institutions.infocamere.InstitutionInfoIC;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.*;
import it.pagopa.selfcare.onboarding.connector.model.registry_proxy.GeographicTaxonomies;
import it.pagopa.selfcare.onboarding.connector.model.registry_proxy.HomogeneousOrganizationalArea;
import it.pagopa.selfcare.onboarding.connector.model.registry_proxy.InstitutionProxyInfo;
import it.pagopa.selfcare.onboarding.connector.model.registry_proxy.OrganizationUnit;
import it.pagopa.selfcare.onboarding.connector.model.user.SaveUserDto;
import it.pagopa.selfcare.onboarding.connector.model.user.UserId;
import it.pagopa.selfcare.onboarding.core.exception.OnboardingNotAllowedException;
import it.pagopa.selfcare.onboarding.core.mapper.InstitutionInfoMapper;
import it.pagopa.selfcare.onboarding.core.mapper.InstitutionInfoMapperImpl;
import it.pagopa.selfcare.onboarding.core.strategy.OnboardingValidationStrategy;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.entity.ProductRole;
import it.pagopa.selfcare.product.entity.ProductRoleInfo;
import it.pagopa.selfcare.product.entity.ProductStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ValidationException;
import java.util.*;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.onboarding.connector.model.user.User.Fields.*;
import static it.pagopa.selfcare.onboarding.core.InstitutionServiceImpl.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstitutionServiceImplTest {

    @InjectMocks
    private InstitutionServiceImpl institutionService;

    @Mock
    private PartyConnector partyConnectorMock;

    @Mock
    private OnboardingMsConnector onboardingMsConnector;

    @Mock
    private ProductsConnector productsConnectorMock;

    @Mock
    private UserRegistryConnector userConnectorMock;

    @Mock
    private OnboardingFunctionsConnector onboardingFunctionsConnector;

    @Mock
    private PartyRegistryProxyConnector partyRegistryProxyConnectorMock;

    @Mock
    private OnboardingValidationStrategy onboardingValidationStrategyMock;

    @Captor
    private ArgumentCaptor<OnboardingData> onboardingDataCaptor;

    @Captor
    private ArgumentCaptor<UserInfo.UserInfoFilter> userInfoFilterCaptor;

    @Spy
    InstitutionInfoMapper institutionInfoMapper = new InstitutionInfoMapperImpl();


    private static final User dummyManager;
    private static final User dummyDelegate;

    static {
        dummyManager = new User();
        dummyManager.setEmail("manager@pec.it");
        dummyManager.setName("manager");
        dummyManager.setSurname("manager");
        dummyManager.setTaxCode("manager");
        dummyManager.setRole(PartyRole.MANAGER);


        dummyDelegate = new User();
        dummyDelegate.setEmail("delegate@pec.it");
        dummyDelegate.setName("delegate");
        dummyDelegate.setSurname("delegate");
        dummyDelegate.setTaxCode("delegate");
        dummyDelegate.setRole(PartyRole.DELEGATE);
    }

    @BeforeEach
    void beforeEach() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void onboarding_nullLocationInfo() {
        //given
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "location");
        onboardingData.setLocation(null);
        onboardingData.setOrigin("ANAC");
        Billing billing = mockInstance(new Billing());
        onboardingData.setBilling(billing);
        //when
        Executable executable = () -> institutionService.onboardingProduct(onboardingData);
        //then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals(LOCATION_INFO_IS_REQUIRED, e.getMessage());
        verifyNoInteractions(productsConnectorMock, partyConnectorMock, userConnectorMock, onboardingValidationStrategyMock);
    }

    @Test
    void onboardingProductAsync() {
        // given
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.PA);
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));
        // when
        institutionService.onboardingProductV2(onboardingData);
        // then
        verify(onboardingMsConnector, times(1))
                .onboarding(any());
    }


    @Test
    void onboardingCompanyV2() {
        // given
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.PG);
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));
        // when
        institutionService.onboardingCompanyV2(onboardingData);
        // then
        verify(onboardingMsConnector, times(1))
                .onboardingCompany(any());
    }

    @Test
    void onboardingPaAggregator() {
        // given
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers", "isAggregator", "aggregates");
        onboardingData.setInstitutionType(InstitutionType.PG);
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));
        onboardingData.setIsAggregator(Boolean.TRUE);
        Institution institution = mock(Institution.class);
        onboardingData.setAggregates(List.of(institution));
        // when
        institutionService.onboardingPaAggregator(onboardingData);
        // then
        verify(onboardingMsConnector, times(1))
                .onboardingPaAggregation(any());
    }

    @Test
    void onboardingPaAggregatorWithEmptyAggregateList() {
        // given
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers", "isAggregator", "aggregates");
        onboardingData.setInstitutionType(InstitutionType.PG);
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));
        onboardingData.setIsAggregator(Boolean.TRUE);
        onboardingData.setAggregates(Collections.emptyList());
        // when
        Assertions.assertThrows(ValidationException.class,
                () -> institutionService.onboardingPaAggregator(onboardingData),
                "Aggregate institutions is request if given institution is an Aggregator");
        // then
        verifyNoInteractions(onboardingMsConnector);
    }

    @Test
    void onboardingPaAggregatorWithNullAggregateList() {
        // given
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers", "isAggregator", "aggregates");
        onboardingData.setInstitutionType(InstitutionType.PG);
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));
        onboardingData.setIsAggregator(Boolean.TRUE);
        // when
        Assertions.assertThrows(ValidationException.class,
                () -> institutionService.onboardingPaAggregator(onboardingData),
                "Aggregate institutions is request if given institution is an Aggregator");
        // then
        verifyNoInteractions(onboardingMsConnector);
    }


    @Test
    void shouldOnboardingProductInstitutionNotPa() {
        // given
        String productRole = "role";

        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.PSP);
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));
        onboardingData.setOrigin("IPA");

        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRole productRole1 = mockInstance(new ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRole productRole2 = mockInstance(new ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};

        Product productMock = new Product();
        productMock.setId(onboardingData.getProductId());
        productMock.setRoleMappings(roleMappings);
        productMock.setParentId("test");

        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());

        when(partyConnectorMock.getInstitutionsByTaxCodeAndSubunitCode(anyString(), anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyRegistryProxyConnectorMock.getInstitutionProxyById(anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitution(any())).thenReturn(institution);

        when(productsConnectorMock.getProduct(any(), any()))
                .thenReturn(productMock);
        when(userConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        institutionService.onboardingProduct(onboardingData);
        // then
        verify(partyConnectorMock, times(1))
                .getInstitutionsByTaxCodeAndSubunitCode(onboardingData.getTaxCode(), onboardingData.getSubunitCode());
        verify(partyConnectorMock, times(1))
                .createInstitution(onboardingData);
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getTaxCode());
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
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
        verify(productsConnectorMock, times(2)).getProduct(any(), any());
        verifyNoMoreInteractions(userConnectorMock, onboardingValidationStrategyMock);
    }

    @Test
    void shouldOnboardingProductInstitutionFromInfocamere() {
        // given
        String productRole = "role";

        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.PG);
        onboardingData.setOrigin(Origin.INFOCAMERE.getValue());
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));

        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRole productRole1 = mockInstance(new ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRole productRole2 = mockInstance(new ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};

        Product productMock = new Product();
        productMock.setId(onboardingData.getProductId());
        productMock.setRoleMappings(roleMappings);
        productMock.setParentId("test");

        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());

        when(partyConnectorMock.getInstitutionsByTaxCodeAndSubunitCode(anyString(), anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionFromInfocamere(any())).thenReturn(institution);

        when(productsConnectorMock.getProduct(any(), any()))
                .thenReturn(productMock);
        when(userConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        institutionService.onboardingProduct(onboardingData);
        // then
        verify(partyConnectorMock, times(1))
                .getInstitutionsByTaxCodeAndSubunitCode(onboardingData.getTaxCode(), onboardingData.getSubunitCode());
        verify(partyConnectorMock, times(1))
                .createInstitutionFromInfocamere(onboardingData);
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getTaxCode());
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
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
        verify(productsConnectorMock, times(2)).getProduct(any(), any());
        verifyNoMoreInteractions(userConnectorMock, onboardingValidationStrategyMock);
    }


    @Test
    void onboardingProductShouldThrowErrorWhenParentProductHasNotValidState() {
        // given
        String productRole = "role";

        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.PSP);
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));
        onboardingData.setOrigin("IPA");

        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRole productRole1 = mockInstance(new ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRole productRole2 = mockInstance(new ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};

        Product productMock = new Product();
        productMock.setId(onboardingData.getProductId());
        productMock.setRoleMappings(roleMappings);
        productMock.setParentId("test");

        Product parentProductMock = new Product();
        parentProductMock.setId(onboardingData.getProductId());
        parentProductMock.setRoleMappings(roleMappings);
        parentProductMock.setStatus(ProductStatus.PHASE_OUT);

        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());

        when(productsConnectorMock.getProduct(any(), any()))
                .thenReturn(productMock)
                .thenReturn(parentProductMock);

        Assertions.assertThrows(ValidationException.class, () -> institutionService.onboardingProduct(onboardingData));
        verify(productsConnectorMock, times(2)).getProduct(any(), any());
    }

    @Test
    void onboardingProductShouldThrowErrorWhenParentProductIsNotOnboarded() {
        // given
        String productRole = "role";

        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.PSP);
        onboardingData.setOrigin("IPA");
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));

        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRole productRole1 = mockInstance(new ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRole productRole2 = mockInstance(new ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};

        Product productMock = new Product();
        productMock.setId(onboardingData.getProductId());
        productMock.setRoleMappings(roleMappings);
        productMock.setParentId("test");

        Product parentProductMock = new Product();
        parentProductMock.setId(onboardingData.getProductId());
        parentProductMock.setRoleMappings(roleMappings);
        parentProductMock.setStatus(ProductStatus.ACTIVE);

        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());

        when(productsConnectorMock.getProduct(any(), any()))
                .thenReturn(productMock)
                .thenReturn(parentProductMock);

        doThrow(new RuntimeException("")).when(partyConnectorMock).verifyOnboarding(any(), any());
        when(onboardingValidationStrategyMock.validate(any(), any())).thenReturn(true);

        Assertions.assertThrows(ValidationException.class, () -> institutionService.onboardingProduct(onboardingData));
        verify(productsConnectorMock, times(2)).getProduct(any(), any());
    }

    @Test
    void shouldOnboardingProductInstitutionPTInvalidProduct() {

        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.PT);
        onboardingData.setProductId(ProductId.PROD_INTEROP.getValue());
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));
        onboardingData.setOrigin("IPA");

        Product product = new Product();
        product.setId("prod-id");
        product.setTitle("title");
        product.setStatus(ProductStatus.ACTIVE);
        product.setDelegable(false);
        when(productsConnectorMock.getProduct(any(), any())).thenReturn(product);

        Assertions.assertThrows(OnboardingNotAllowedException.class, () -> institutionService.onboardingProduct(onboardingData));
    }

    @Test
    void shouldOnboardingProductInstitutionPa() {
        // given
        String productRole = "role";

        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.PA);
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));
        onboardingData.setOrigin("IPA");
        Product productMock = new Product();
        productMock.setId(onboardingData.getProductId());
        ProductRoleInfo productRoleInfo1 = mockInstance(new ProductRoleInfo());
        ProductRole productRole1 = new ProductRole();
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = mockInstance(new ProductRoleInfo());
        ProductRole productRole2 = new ProductRole();
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        Institution institution = new Institution();
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.getInstitutionsByTaxCodeAndSubunitCode(anyString(), anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionFromIpa(any(), any(), any()))
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
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        institutionService.onboardingProduct(onboardingData);
        // then
        verify(partyConnectorMock, times(1))
                .getInstitutionsByTaxCodeAndSubunitCode(onboardingData.getTaxCode(), onboardingData.getSubunitCode());
        verify(partyConnectorMock, times(1))
                .createInstitutionFromIpa(onboardingData.getTaxCode(), onboardingData.getSubunitCode(), onboardingData.getSubunitType());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getTaxCode());
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
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
    }

    @Test
    void shouldOnboardingProductInstitutionAnac() {
        // given
        String productRole = "role";

        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.SA);
        onboardingData.setOrigin("ANAC");
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));
        Product productMock = new Product();
        productMock.setId(onboardingData.getProductId());
        ProductRoleInfo productRoleInfo1 = new ProductRoleInfo();
        ProductRole productRole1 = new ProductRole();
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = new ProductRoleInfo();
        ProductRole productRole2 = new ProductRole();
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.getInstitutionsByTaxCodeAndSubunitCode(anyString(), anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionFromANAC(any()))
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
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        institutionService.onboardingProduct(onboardingData);
        // then
        verify(partyConnectorMock, times(1))
                .getInstitutionsByTaxCodeAndSubunitCode(onboardingData.getTaxCode(), onboardingData.getSubunitCode());
        verify(partyConnectorMock, times(1))
                .createInstitutionFromANAC(onboardingData);
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getTaxCode());
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
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
    }

    @Test
    void shouldOnboardingProductInstitutionIvass() {
        // given
        String productRole = "role";

        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.AS);
        onboardingData.setOrigin("IVASS");
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));
        Product productMock = new Product();
        productMock.setId(onboardingData.getProductId());
        ProductRoleInfo productRoleInfo1 = new ProductRoleInfo();
        ProductRole productRole1 = new ProductRole();
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = new ProductRoleInfo();
        ProductRole productRole2 = new ProductRole();
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.getInstitutionsByTaxCodeAndSubunitCode(anyString(), anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionFromIVASS(any()))
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
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        institutionService.onboardingProduct(onboardingData);
        // then
        verify(partyConnectorMock, times(1))
                .getInstitutionsByTaxCodeAndSubunitCode(onboardingData.getTaxCode(), onboardingData.getSubunitCode());
        verify(partyConnectorMock, times(1))
                .createInstitutionFromIVASS(onboardingData);
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getTaxCode());
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
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
    }

    @Test
    void shouldOnboardingProductInstitutionSA() {
        // given
        String productRole = "role";

        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.SA);
        onboardingData.setOrigin("IPA");
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));
        Product productMock = new Product();
        productMock.setId(onboardingData.getProductId());
        ProductRoleInfo productRoleInfo1 = new ProductRoleInfo();
        ProductRole productRole1 = new ProductRole();
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = new ProductRoleInfo();
        ProductRole productRole2 = new ProductRole();
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.getInstitutionsByTaxCodeAndSubunitCode(anyString(), anyString()))
                .thenThrow(ResourceNotFoundException.class);
        when(partyConnectorMock.createInstitutionFromIpa(any(), any(), any()))
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
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        // when
        institutionService.onboardingProduct(onboardingData);
        // then
        verify(partyConnectorMock, times(1))
                .getInstitutionsByTaxCodeAndSubunitCode(onboardingData.getTaxCode(), onboardingData.getSubunitCode());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getTaxCode());
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
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
    }

    @Test
    void shouldOnboardingProductInstitutionIpaAOO() {
        // given
        String productRole = "role";

        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.SA);
        onboardingData.setOrigin("IPA");
        onboardingData.setSubunitType("AOO");
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));
        Product productMock = new Product();
        productMock.setId(onboardingData.getProductId());
        ProductRoleInfo productRoleInfo1 = new ProductRoleInfo();
        ProductRole productRole1 = new ProductRole();
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = new ProductRoleInfo();
        ProductRole productRole2 = new ProductRole();
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.getInstitutionsByTaxCodeAndSubunitCode(anyString(), anyString()))
                .thenThrow(ResourceNotFoundException.class);
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(productMock);
        when(userConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        when(partyRegistryProxyConnectorMock.getAooById(anyString()))
                .thenReturn(mockInstance(new HomogeneousOrganizationalArea()));
        when(partyConnectorMock.createInstitutionFromIpa(any(), any(), any()))
                .thenReturn(institution);

        // when
        institutionService.onboardingProduct(onboardingData);
        // then
        verify(partyConnectorMock, times(1))
                .getInstitutionsByTaxCodeAndSubunitCode(onboardingData.getTaxCode(), onboardingData.getSubunitCode());
        verify(partyConnectorMock, times(1))
                .createInstitutionFromIpa(onboardingData.getTaxCode(),onboardingData.getSubunitCode(), onboardingData.getSubunitType());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getTaxCode());
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
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
    }

    @Test
    void shouldOnboardingProductInstitutionIpaUO() {
        // given
        String productRole = "role";

        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setInstitutionType", "setUsers");
        onboardingData.setInstitutionType(InstitutionType.SA);
        onboardingData.setOrigin("IPA");
        onboardingData.setSubunitType("UO");
        onboardingData.setUsers(List.of(dummyManager, dummyDelegate));
        Product productMock = new Product();
        productMock.setId(onboardingData.getProductId());
        ProductRoleInfo productRoleInfo1 = new ProductRoleInfo();
        ProductRole productRole1 = new ProductRole();
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = new ProductRoleInfo();
        ProductRole productRole2 = new ProductRole();
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        Institution institution = mockInstance(new Institution());
        institution.setId(UUID.randomUUID().toString());
        when(partyConnectorMock.getInstitutionsByTaxCodeAndSubunitCode(anyString(), anyString()))
                .thenThrow(ResourceNotFoundException.class);
        productMock.setRoleMappings(roleMappings);
        when(productsConnectorMock.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType()))
                .thenReturn(productMock);
        when(userConnectorMock.saveUser(any()))
                .thenAnswer(invocation -> {
                    UserId userId = new UserId();
                    userId.setId(UUID.randomUUID());
                    return userId;
                });
        when(onboardingValidationStrategyMock.validate(any(), any()))
                .thenReturn(true);
        when(partyRegistryProxyConnectorMock.getUoById(anyString()))
                .thenReturn(mockInstance(new OrganizationUnit()));
        when(partyConnectorMock.createInstitutionFromIpa(any(), any(), any()))
                .thenReturn(institution);

        // when
        institutionService.onboardingProduct(onboardingData);
        // then
        verify(partyConnectorMock, times(1))
                .getInstitutionsByTaxCodeAndSubunitCode(onboardingData.getTaxCode(), onboardingData.getSubunitCode());
        verify(partyConnectorMock, times(1))
                .createInstitutionFromIpa(onboardingData.getTaxCode(),onboardingData.getSubunitCode(), onboardingData.getSubunitType());
        verify(productsConnectorMock, times(1))
                .getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(onboardingData.getProductId(), onboardingData.getTaxCode());
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
        assertNotNull(captured.getUsers());
        assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> {
            assertEquals(productRole, userInfo.getProductRole());
            assertNotNull(userInfo.getId());
        });
    }

    @Test
    void getInstitutions() {
        //given
        InstitutionInfo expectedInstitutionInfo = new InstitutionInfo();
        final String userId = "userId";
        when(partyConnectorMock.getInstitutionsByUser(null, userId))
                .thenReturn(List.of(expectedInstitutionInfo));
        // when
        Collection<InstitutionInfo> institutions = institutionService.getInstitutions(null, userId);
        // then
        assertNotNull(institutions);
        assertEquals(1, institutions.size());
        assertSame(expectedInstitutionInfo, institutions.iterator().next());
        verify(partyConnectorMock, times(1))
                .getInstitutionsByUser(null, userId);
        verifyNoMoreInteractions(partyConnectorMock);
        verifyNoInteractions(productsConnectorMock, userConnectorMock);
    }

    @Test
    void getInstitutions_emptyResult() {
        //given
        final String userId = "userId";
        //when
        Collection<InstitutionInfo> institutions = institutionService.getInstitutions(null, userId);
        // then
        assertNotNull(institutions);
        assertTrue(institutions.isEmpty());
        verify(partyConnectorMock, times(1))
                .getInstitutionsByUser(null, userId);
        verifyNoMoreInteractions(partyConnectorMock);
        verifyNoInteractions(productsConnectorMock, userConnectorMock);
    }

    @Test
    void getInstitutionByExternalId_nullInstitutionId() {
        //when
        Executable executable = () -> institutionService.getInstitutionByExternalId(null);
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
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
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

    @Test
    void getGeographicTaxonomyListByTaxCode_shouldEmptyListIfInstitutionNotFound() {
        // given
        String taxCode = "taxCode";
        when(partyConnectorMock.getInstitutionsByTaxCodeAndSubunitCode(taxCode, null))
                .thenReturn(List.of());
        // when
        List<GeographicTaxonomy> result = institutionService.getGeographicTaxonomyList(taxCode, null);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getGeographicTaxonomyListByTaxCode_shouldEmptyListIfGeoNotPresent() {
        // given
        String taxCode = "taxCode";
        String subunitCode = "subunitCode";
        Institution institutionMock = mockInstance(new Institution());
        when(partyConnectorMock.getInstitutionsByTaxCodeAndSubunitCode(taxCode, subunitCode))
                .thenReturn(List.of(institutionMock));
        // when
        List<GeographicTaxonomy> result = institutionService.getGeographicTaxonomyList(taxCode, subunitCode);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getGeographicTaxonomyListByTaxCode() {
        // given
        String taxCode = "taxCode";
        String subunitCode = "subunitCode";
        Institution institutionMock = mockInstance(new Institution());
        institutionMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        when(partyConnectorMock.getInstitutionsByTaxCodeAndSubunitCode(taxCode, subunitCode))
                .thenReturn(List.of(institutionMock));
        // when
        List<GeographicTaxonomy> result = institutionService.getGeographicTaxonomyList(taxCode, subunitCode);

        GeographicTaxonomy expected = institutionMock.getGeographicTaxonomies().get(0);
        // then
        assertNotNull(result);
        assertEquals(expected.getCode(), result.get(0).getCode());
        assertEquals(expected.getDesc(), result.get(0).getDesc());
    }

    @Test
    void shouldGeographicTaxonomyListEmptyWhenInstitutionGeoListIsNull() {
        // given
        String institutionId = "institutionId";
        Institution institutionMock = mockInstance(new Institution());
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenReturn(institutionMock);
        // when
        List<GeographicTaxonomy> result = institutionService.getGeographicTaxonomyList(institutionId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(institutionId);
        verifyNoMoreInteractions(partyConnectorMock);
        verifyNoInteractions(productsConnectorMock, userConnectorMock);
    }

    @Test
    void verifyOnboarding_notAllowed() {
        // given
        final String externalInstitutionId = "externalInstitutionId";
        final String productId = "productId";
        // when
        final Executable executable = () -> institutionService.verifyOnboarding(externalInstitutionId, productId);
        // then
        final Exception e = assertThrows(OnboardingNotAllowedException.class, executable);
        assertEquals("Institution with external id '" + externalInstitutionId + "' is not allowed to onboard '" + productId + "' product", e.getMessage());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(productId, externalInstitutionId);
        verifyNoMoreInteractions(onboardingValidationStrategyMock);
        verifyNoInteractions(productsConnectorMock, userConnectorMock, partyConnectorMock);
    }


    @Test
    void verifyOnboarding_allowed() {
        // given
        final String externalInstitutionId = "externalInstitutionId";
        final String productId = "productId";
        when(onboardingValidationStrategyMock.validate(productId, externalInstitutionId))
                .thenReturn(true);
        // when
        final Executable executable = () -> institutionService.verifyOnboarding(externalInstitutionId, productId);
        // then
        assertDoesNotThrow(executable);
        verify(onboardingValidationStrategyMock, times(1))
                .validate(productId, externalInstitutionId);
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(externalInstitutionId, productId);
        verifyNoMoreInteractions(onboardingValidationStrategyMock, partyConnectorMock);
        verifyNoInteractions(productsConnectorMock, userConnectorMock);
    }

    @Test
    void verifyOnboardingInfo_notAllowed() {
        // given
        final String taxCode = "taxCode";
        final String subunitCode = "subunitCode";
        final String productId = "productId";
        final String externalId = "externalId";
        final String origin = "origin";
        final String originId = "originId";
        // when
        final Executable executable = () -> institutionService.verifyOnboarding(productId, externalId, taxCode, origin, originId, subunitCode);
        // then
        final Exception e = assertThrows(OnboardingNotAllowedException.class, executable);
        assertEquals("Institution with external id '" + taxCode + "' is not allowed to onboard '" + productId + "' product", e.getMessage());
        verify(onboardingValidationStrategyMock, times(1))
                .validate(productId, taxCode);
        verifyNoMoreInteractions(onboardingValidationStrategyMock);
        verifyNoInteractions(productsConnectorMock, userConnectorMock, partyConnectorMock);
    }

    @Test
    void verifyOnboardingInfo_allowed() {
        // given
        final String taxCode = "taxCode";
        final String subunitCode = "subunitCode";
        final String productId = "productId";
        final String externalId = "externalId";
        final String origin = "origin";
        final String originId = "originId";
        when(onboardingValidationStrategyMock.validate(productId, taxCode))
                .thenReturn(true);
        // when
        final Executable executable = () -> institutionService.verifyOnboarding(productId, externalId, taxCode, origin, originId, subunitCode);
        // then
        assertDoesNotThrow(executable);
        verify(onboardingValidationStrategyMock, times(1))
                .validate(productId, taxCode);
        verify(partyConnectorMock, times(1))
                .verifyOnboarding(productId, externalId, taxCode, origin, originId, subunitCode);
        verifyNoMoreInteractions(onboardingValidationStrategyMock, partyConnectorMock);
        verifyNoInteractions(productsConnectorMock, userConnectorMock);
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
        List<BusinessInfoIC> businessInfoICSmock = List.of(mockInstance(new BusinessInfoIC()));
        InstitutionInfoIC institutionInfoICmock = mockInstance(new InstitutionInfoIC(), "setBusinesses");
        institutionInfoICmock.setBusinesses(businessInfoICSmock);
        when(partyRegistryProxyConnectorMock.getInstitutionsByUserFiscalCode(anyString()))
                .thenReturn(institutionInfoICmock);
        //when
        InstitutionInfoIC result = institutionService.getInstitutionsByUser(user.getTaxCode());
        //then
        assertNotNull(result);
        assertEquals(institutionInfoICmock.getBusinesses().get(0).getBusinessName(), result.getBusinesses().get(0).getBusinessName());
        assertEquals(institutionInfoICmock.getBusinesses().get(0).getBusinessTaxId(), result.getBusinesses().get(0).getBusinessTaxId());
        assertEquals(institutionInfoICmock.getLegalTaxId(), result.getLegalTaxId());
        assertEquals(institutionInfoICmock.getRequestDateTime(), result.getRequestDateTime());
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
        MatchInfoResult matchInfo = mockInstance(new MatchInfoResult());
        when(partyRegistryProxyConnectorMock.matchInstitutionAndUser(anyString(), anyString()))
                .thenReturn(matchInfo);
        //when
        MatchInfoResult result = institutionService.matchInstitutionAndUser(externalId, user);
        //then
        assertNotNull(result);
        assertEquals(result.isVerificationResult(), matchInfo.isVerificationResult());
        verify(partyRegistryProxyConnectorMock, times(1))
                .matchInstitutionAndUser(externalId, taxCode);
        verifyNoMoreInteractions(partyRegistryProxyConnectorMock);

    }

    @Test
    void getByFilters() {
        //given
        final String subunitCode = "subunitCode";
        final String taxCode = "taxCode";
        final String productId = "productId";
        OnboardingData onboardingData = new OnboardingData();
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setDescription("description");
        onboardingData.setInstitutionUpdate(institutionUpdate);
        when(onboardingMsConnector.getByFilters(productId, taxCode, null, null, subunitCode))
                .thenReturn(List.of(onboardingData));
        //when
        List<Institution> result = institutionService.getByFilters(productId, taxCode, null, null, subunitCode);
        //then
        assertNotNull(result);
        assertEquals(result.get(0).getDescription(), institutionUpdate.getDescription());
        verify(onboardingMsConnector, times(1))
                .getByFilters(productId, taxCode, null, null, subunitCode);
        verifyNoMoreInteractions(onboardingMsConnector);

    }

    @Test
    void getByFiltersTooManyResource() {
        //given
        final String subunitCode = "subunitCode";
        final String taxCode = "taxCode";
        final String productId = "productId";
        OnboardingData onboardingData = new OnboardingData();
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setDescription("description");
        onboardingData.setInstitutionUpdate(institutionUpdate);
        when(onboardingMsConnector.getByFilters(productId, taxCode, null, null, subunitCode))
                .thenReturn(List.of());
        //when
        Executable executable = () -> institutionService.getByFilters(productId, taxCode, null, null, subunitCode);
        //then
        assertThrows(ResourceNotFoundException.class, executable);
    }

    @Test
    void getInstitutionLegalAddress() {
        //given
        String externalId = "externalId";
        InstitutionLegalAddressData data = mockInstance(new InstitutionLegalAddressData());
        when(partyRegistryProxyConnectorMock.getInstitutionLegalAddress(anyString()))
                .thenReturn(data);
        //when
        InstitutionLegalAddressData result = institutionService.getInstitutionLegalAddress(externalId);
        //then
        assertNotNull(result);
        assertEquals(result.getAddress(), data.getAddress());
        assertEquals(result.getZipCode(), data.getZipCode());
        verify(partyRegistryProxyConnectorMock, times(1))
                .getInstitutionLegalAddress(externalId);
        verifyNoMoreInteractions(partyRegistryProxyConnectorMock);

    }

    @Test
    void checkOrganization() {
        //given
        final String fiscalCode = "fiscalCode";
        final String vatNumber = "vatNumber";

        //when
        Executable executable = () -> institutionService.checkOrganization(null, fiscalCode, vatNumber);
        //then
        assertDoesNotThrow(executable);
        verify(onboardingFunctionsConnector, times(1)).checkOrganization(fiscalCode, vatNumber);
        verifyNoMoreInteractions(onboardingFunctionsConnector);
    }

    @Test
    void getInstitutionOnboardingDataById_nullInstitutionId() {
        //given
        String institutionId = null;
        String productId = "productId";
        //when
        Executable executable = () -> institutionService.getInstitutionOnboardingDataById(institutionId, productId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_ID_MESSAGE, e.getMessage());
        verifyNoInteractions(partyConnectorMock, productsConnectorMock, userConnectorMock);
    }
    @Test
    void getInstitutionOnboardingDataById_nullProductId() {
        //given
        String institutionId = "institutionId";
        String productId = null;
        //when
        Executable executable = () -> institutionService.getInstitutionOnboardingDataById(institutionId, productId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(A_PRODUCT_ID_IS_REQUIRED, e.getMessage());
        verifyNoInteractions(partyConnectorMock, productsConnectorMock, userConnectorMock);
    }
    @Test
    void getInstitutionOnboardingDataById_onboardingsNotFound() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";

        //when
        Executable executable = () -> institutionService.getInstitutionOnboardingDataById(institutionId, productId);
        //then
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, executable);
        assertEquals(String.format("Onboarding for institutionId %s not found", institutionId), e.getMessage());

        verify(partyConnectorMock, times(1))
                .getOnboardings(institutionId, productId);

        verifyNoMoreInteractions(partyConnectorMock, userConnectorMock);
        verifyNoInteractions(productsConnectorMock);
    }

    @Test
    void getInstitutionOnboardingDataById() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";

        OnboardingResource onboardingResource = new OnboardingResource();
        onboardingResource.setProductId(productId);
        when(partyConnectorMock.getOnboardings(institutionId, productId))
                .thenReturn(List.of(onboardingResource));

        Institution institutionMock = mockInstance(new Institution());
        institutionMock.setGeographicTaxonomies(List.of(new GeographicTaxonomy()));
        when(partyConnectorMock.getInstitutionById(institutionId))
                .thenReturn(institutionMock);
        //when
        InstitutionOnboardingData institutionOnboardingData = institutionService.getInstitutionOnboardingDataById(institutionId, productId);
        //then
        assertNotNull(institutionOnboardingData);

        assertNotNull(institutionOnboardingData.getInstitution());
        assertEquals(institutionMock.getInstitutionType(), institutionOnboardingData.getInstitution().getInstitutionType());
        assertEquals(institutionMock.getTaxCode(), institutionOnboardingData.getInstitution().getTaxCode());
        assertEquals(institutionMock.getZipCode(), institutionOnboardingData.getInstitution().getZipCode());
        assertEquals(institutionMock.getDescription(), institutionOnboardingData.getInstitution().getDescription());
        assertEquals(institutionMock.getGeographicTaxonomies().get(0).getCode(), institutionOnboardingData.getGeographicTaxonomies().get(0).getCode());
        assertEquals(institutionMock.getGeographicTaxonomies().get(0).getDesc(), institutionOnboardingData.getGeographicTaxonomies().get(0).getDesc());

        verify(partyConnectorMock, times(1))
                .getOnboardings(institutionId, productId);

        verify(partyConnectorMock, times(1))
                .getInstitutionById(institutionId);
        verifyNoMoreInteractions(partyConnectorMock, userConnectorMock);
        verifyNoInteractions(productsConnectorMock);
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
    void getInstitutionOnboardingData_nullProductId() {
        //given
        String institutionId = "institutionId";
        String productId = null;
        //when
        Executable executable = () -> institutionService.getInstitutionOnboardingData(institutionId, productId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(A_PRODUCT_ID_IS_REQUIRED, e.getMessage());
        verifyNoInteractions(partyConnectorMock, productsConnectorMock, userConnectorMock);
    }

    @Test
    void getInstitutionOnboardingData_institutionNotFound() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";

        //when
        Executable executable = () -> institutionService.getInstitutionOnboardingData(institutionId, productId);
        //then
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, executable);
        assertEquals(String.format("Institution %s not found", institutionId), e.getMessage());

        verify(partyConnectorMock, times(1))
                .getInstitutionBillingData(institutionId, productId);

        verifyNoMoreInteractions(partyConnectorMock, userConnectorMock);
        verifyNoInteractions(productsConnectorMock);
    }

    @Test
    void getInstitutionOnboardingData_institutionByExternalIdNotFound() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";

        when(partyConnectorMock.getInstitutionBillingData(any(), any())).thenReturn(new InstitutionInfo());

        //when
        Executable executable = () -> institutionService.getInstitutionOnboardingData(institutionId, productId);
        //then
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, executable);
        assertEquals(String.format("Institution %s not found", institutionId), e.getMessage());

        verify(partyConnectorMock, times(1))
                .getInstitutionBillingData(institutionId, productId);

        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(institutionId);
        verifyNoMoreInteractions(partyConnectorMock, userConnectorMock);
        verifyNoInteractions(productsConnectorMock);
    }

    @Test
    void getInstitutionOnboardingData_nullGeographicTaxonomies() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";

        InstitutionInfo institutionInfoMock = mockInstance(new InstitutionInfo());
        Billing billingMock = mockInstance(new Billing());
        institutionInfoMock.setBilling(billingMock);
        when(partyConnectorMock.getInstitutionBillingData(anyString(), anyString()))
                .thenReturn(institutionInfoMock);
        Institution institutionMock = mockInstance(new Institution());
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenReturn(institutionMock);
        //when
        Executable executable = () -> institutionService.getInstitutionOnboardingData(institutionId, productId);
        //then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals(String.format("The institution %s does not have geographic taxonomies.", institutionId), e.getMessage());

        verify(partyConnectorMock, times(1))
                .getInstitutionBillingData(institutionId, productId);

        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(institutionId);
        verifyNoMoreInteractions(partyConnectorMock, userConnectorMock);
        verifyNoInteractions(productsConnectorMock);
    }

    @Test
    void getInstitutionOnboardingData_NoLocation() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        String loggedUser = "loggedUser";
        UserInfo userInfoMock = mockInstance(new UserInfo(), "setId", "setRole");
        userInfoMock.setId(loggedUser);
        final InstitutionInfo institutionInfoMock = createInstitutionInfoMock();
        Billing billingMock = mockInstance(new Billing());
        institutionInfoMock.setBilling(billingMock);
        when(partyConnectorMock.getInstitutionBillingData(anyString(), anyString()))
                .thenReturn(institutionInfoMock);
        Institution institutionMock = mockInstance(new Institution(), "setCity");
        institutionMock.setSubunitType(null);
        institutionMock.setOrigin("IPA");
        institutionMock.setGeographicTaxonomies(List.of(new GeographicTaxonomy()));
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenReturn(institutionMock);
        InstitutionProxyInfo proxyInstitution = new InstitutionProxyInfo();
        proxyInstitution.setIstatCode("istatCode");
        when(partyRegistryProxyConnectorMock.getInstitutionProxyById(anyString())).thenReturn(proxyInstitution);
        GeographicTaxonomies geographicTaxonomies = mockInstance(new GeographicTaxonomies());
        when(partyRegistryProxyConnectorMock.getExtById(anyString())).thenReturn(geographicTaxonomies);
        //when
        InstitutionOnboardingData institutionOnboardingData = institutionService.getInstitutionOnboardingData(institutionId, productId);
        //then
        assertNotNull(institutionOnboardingData);
        assertNotNull(institutionOnboardingData.getInstitution().getInstitutionLocation());
        assertNotNull(institutionOnboardingData.getInstitution().getInstitutionLocation().getCity());

    }
    @Test
    void getInstitutionOnboardingData_NoLocationEC() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        String loggedUser = "loggedUser";
        UserInfo userInfoMock = mockInstance(new UserInfo(), "setId", "setRole");
        userInfoMock.setId(loggedUser);
        final InstitutionInfo institutionInfoMock = createInstitutionInfoMock();
        Billing billingMock = mockInstance(new Billing());
        institutionInfoMock.setBilling(billingMock);
        when(partyConnectorMock.getInstitutionBillingData(anyString(), anyString()))
                .thenReturn(institutionInfoMock);
        Institution institutionMock = mockInstance(new Institution(), "setCity");
        institutionMock.setSubunitType("EC");
        institutionMock.setOrigin("IPA");
        institutionMock.setGeographicTaxonomies(List.of(new GeographicTaxonomy()));
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenReturn(institutionMock);
        InstitutionProxyInfo proxyInstitution = new InstitutionProxyInfo();
        proxyInstitution.setIstatCode("istatCode");
        when(partyRegistryProxyConnectorMock.getInstitutionProxyById(anyString())).thenReturn(proxyInstitution);
        GeographicTaxonomies geographicTaxonomies = mockInstance(new GeographicTaxonomies());
        when(partyRegistryProxyConnectorMock.getExtById(anyString())).thenReturn(geographicTaxonomies);
        //when
        InstitutionOnboardingData institutionOnboardingData = institutionService.getInstitutionOnboardingData(institutionId, productId);
        //then
        assertNotNull(institutionOnboardingData);
        assertNotNull(institutionOnboardingData.getInstitution().getInstitutionLocation());
        assertNotNull(institutionOnboardingData.getInstitution().getInstitutionLocation().getCity());

    }
    @Test
    void getInstitutionOnboardingData_NoLocationAOO() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        String loggedUser = "loggedUser";
        UserInfo userInfoMock = mockInstance(new UserInfo(), "setId", "setRole");
        userInfoMock.setId(loggedUser);
        final InstitutionInfo institutionInfoMock = createInstitutionInfoMock();
        Billing billingMock = mockInstance(new Billing());
        institutionInfoMock.setBilling(billingMock);
        when(partyConnectorMock.getInstitutionBillingData(anyString(), anyString()))
                .thenReturn(institutionInfoMock);
        Institution institutionMock = mockInstance(new Institution(), "setCity");
        institutionMock.setSubunitType("AOO");
        institutionMock.setOrigin("IPA");
        institutionMock.setGeographicTaxonomies(List.of(new GeographicTaxonomy()));
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenReturn(institutionMock);
        when(partyRegistryProxyConnectorMock.getAooById(anyString())).thenReturn(mockInstance(new HomogeneousOrganizationalArea()));
        GeographicTaxonomies geographicTaxonomies = mockInstance(new GeographicTaxonomies());
        when(partyRegistryProxyConnectorMock.getExtById(anyString())).thenReturn(geographicTaxonomies);
        //when
        InstitutionOnboardingData institutionOnboardingData = institutionService.getInstitutionOnboardingData(institutionId, productId);
        //then
        assertNotNull(institutionOnboardingData);
        assertNotNull(institutionOnboardingData.getInstitution().getInstitutionLocation());
        assertNotNull(institutionOnboardingData.getInstitution().getInstitutionLocation().getCity());
    }
    @Test
    void getInstitutionOnboardingData_NoLocationUO() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        String loggedUser = "loggedUser";
        UserInfo userInfoMock = mockInstance(new UserInfo(), "setId", "setRole");
        userInfoMock.setId(loggedUser);
        final InstitutionInfo institutionInfoMock = createInstitutionInfoMock();
        Billing billingMock = mockInstance(new Billing());
        institutionInfoMock.setBilling(billingMock);
        when(partyConnectorMock.getInstitutionBillingData(anyString(), anyString()))
                .thenReturn(institutionInfoMock);
        Institution institutionMock = mockInstance(new Institution(), "setCity");
        institutionMock.setSubunitType("UO");
        institutionMock.setOrigin("IPA");
        institutionMock.setGeographicTaxonomies(List.of(new GeographicTaxonomy()));
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenReturn(institutionMock);
        when(partyRegistryProxyConnectorMock.getUoById(anyString())).thenReturn(mockInstance(new OrganizationUnit()));
        GeographicTaxonomies geographicTaxonomies = mockInstance(new GeographicTaxonomies());
        when(partyRegistryProxyConnectorMock.getExtById(anyString())).thenReturn(geographicTaxonomies);
        //when
        InstitutionOnboardingData institutionOnboardingData = institutionService.getInstitutionOnboardingData(institutionId, productId);
        //then
        assertNotNull(institutionOnboardingData);
        assertNotNull(institutionOnboardingData.getInstitution().getInstitutionLocation());
        assertNotNull(institutionOnboardingData.getInstitution().getInstitutionLocation().getCity());

    }

    @Test
    void getOnboardingData_locationNotFound(){
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        String loggedUser = "loggedUser";
        UserInfo userInfoMock = mockInstance(new UserInfo(), "setId", "setRole");
        userInfoMock.setId(loggedUser);
        final InstitutionInfo institutionInfoMock = createInstitutionInfoMock();
        Billing billingMock = mockInstance(new Billing());
        institutionInfoMock.setBilling(billingMock);
        when(partyConnectorMock.getInstitutionBillingData(anyString(), anyString()))
                .thenReturn(institutionInfoMock);
        Institution institutionMock = mockInstance(new Institution(), "setCity");
        institutionMock.setSubunitType("EC");
        institutionMock.setOrigin("IPA");
        institutionMock.setGeographicTaxonomies(List.of(new GeographicTaxonomy()));
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenReturn(institutionMock);
        InstitutionProxyInfo proxyInstitution = new InstitutionProxyInfo();
        proxyInstitution.setIstatCode("istatCode");
        when(partyRegistryProxyConnectorMock.getInstitutionProxyById(anyString())).thenThrow(ResourceNotFoundException.class);
        //when
        InstitutionOnboardingData institutionOnboardingData = institutionService.getInstitutionOnboardingData(institutionId, productId);
        //then
        assertNotNull(institutionOnboardingData);
        assertNotNull(institutionOnboardingData.getInstitution().getInstitutionLocation());
        assertNull(institutionOnboardingData.getInstitution().getInstitutionLocation().getCity());
    }
    @Test
    void getInstitutionOnboardingData() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        String loggedUser = "loggedUser";
        UserInfo userInfoMock = mockInstance(new UserInfo(), "setId", "setRole");
        userInfoMock.setId(loggedUser);

        InstitutionInfo institutionInfoMock = mockInstance(new InstitutionInfo());
        Billing billingMock = mockInstance(new Billing());
        institutionInfoMock.setBilling(billingMock);
        when(partyConnectorMock.getInstitutionBillingData(anyString(), anyString()))
                .thenReturn(institutionInfoMock);
        Institution institutionMock = mockInstance(new Institution());
        institutionMock.setGeographicTaxonomies(List.of(new GeographicTaxonomy()));
        when(partyConnectorMock.getInstitutionByExternalId(anyString()))
                .thenReturn(institutionMock);
        //when
        InstitutionOnboardingData institutionOnboardingData = institutionService.getInstitutionOnboardingData(institutionId, productId);
        //then
        assertNotNull(institutionOnboardingData);

        assertNotNull(institutionOnboardingData.getInstitution());
        assertEquals(institutionInfoMock, institutionOnboardingData.getInstitution());
        assertEquals(institutionMock.getGeographicTaxonomies().get(0).getCode(), institutionOnboardingData.getGeographicTaxonomies().get(0).getCode());
        assertEquals(institutionMock.getGeographicTaxonomies().get(0).getDesc(), institutionOnboardingData.getGeographicTaxonomies().get(0).getDesc());

        verify(partyConnectorMock, times(1))
                .getInstitutionBillingData(institutionId, productId);

        verify(partyConnectorMock, times(1))
                .getInstitutionByExternalId(institutionId);
        verifyNoMoreInteractions(partyConnectorMock, userConnectorMock);
        verifyNoInteractions(productsConnectorMock);
    }

    @Test
    void validateAggregatesCsvReturnsValidResultWhenNoErrors() {

        MultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());
        VerifyAggregateResult expected = new VerifyAggregateResult();
        expected.setAggregates(Arrays.asList(new Institution(), new Institution()));
        expected.setErrors(Collections.emptyList());
        when(onboardingMsConnector.verifyAggregatesCsv(any(MultipartFile.class))).thenReturn(expected);

        // When
        VerifyAggregateResult result = institutionService.validateAggregatesCsv(file);

        // Then
        assertEquals(0, result.getErrors().size());
        verify(onboardingMsConnector, times(1)).verifyAggregatesCsv(any(MultipartFile.class));
        verifyNoMoreInteractions(onboardingMsConnector);
    }

    @Test
    void validateAggregatesCsvReturnsValidResultWhenErrorsExist() {
        // Given
        MultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());
        VerifyAggregateResult expected = new VerifyAggregateResult();
        expected.setErrors(Arrays.asList(new RowError(), new RowError()));
        when(onboardingMsConnector.verifyAggregatesCsv(any(MultipartFile.class))).thenReturn(expected);

        // When
        VerifyAggregateResult result = institutionService.validateAggregatesCsv(file);

        // Then
        assertEquals(0, result.getAggregates().size());
        verify(onboardingMsConnector, times(1)).verifyAggregatesCsv(any(MultipartFile.class));
        verifyNoMoreInteractions(onboardingMsConnector);
    }

    @Test
    void checkRecipientCode() {
        // Given
        when(onboardingMsConnector.checkRecipientCode(any(), any(), any())).thenReturn(RecipientCodeStatusResult.ACCEPTED);

        // When
        RecipientCodeStatusResult result = institutionService.checkRecipientCode("originId", "subunitCode", "recipientCode");

        // Then
        assertEquals(result, RecipientCodeStatusResult.ACCEPTED);
        verify(onboardingMsConnector, times(1)).checkRecipientCode(any(), any(), any());
        verifyNoMoreInteractions(onboardingMsConnector);
    }


    private static InstitutionInfo createInstitutionInfoMock() {
        return false ? mockInstance(new InstitutionInfo()) : mockInstance(new InstitutionInfo(), "setInstitutionLocation");
    }
}