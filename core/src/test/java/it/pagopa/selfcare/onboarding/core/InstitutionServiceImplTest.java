package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.api.PartyConnector;
import it.pagopa.selfcare.onboarding.connector.api.ProductsConnector;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipInfo;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipsResponse;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Attribute;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.*;
import it.pagopa.selfcare.onboarding.connector.model.product.Product;
import it.pagopa.selfcare.onboarding.connector.model.product.ProductRoleInfo;
import it.pagopa.selfcare.onboarding.core.exceptions.InternalServerException;
import it.pagopa.selfcare.onboarding.core.exceptions.ProductHasNoRelationshipException;
import it.pagopa.selfcare.onboarding.core.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.TestSecurityContextHolder;

import javax.validation.ValidationException;
import java.util.*;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.onboarding.connector.model.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.onboarding.core.InstitutionServiceImpl.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InstitutionServiceImplTest {

    @InjectMocks
    private InstitutionServiceImpl institutionService;

    @Mock
    private PartyConnector partyConnectorMock;

    @Mock
    private ProductsConnector productsConnectorMock;

    @Captor
    ArgumentCaptor<OnboardingData> onboardingDataCaptor;

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
        Mockito.verifyNoInteractions(partyConnectorMock, productsConnectorMock);
    }


    @Test
    void onboarding_nullRoleMapping() {
        // given
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        Product product = TestUtils.mockInstance(new Product(), "setParentId");
        Mockito.when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(product);
        // when
        Executable executable = () -> institutionService.onboarding(onboardingData);
        // then
        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("Role mappings is required", e.getMessage());
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProduct(onboardingData.getProductId());
        Mockito.verifyNoMoreInteractions(productsConnectorMock);
        Mockito.verifyNoInteractions(partyConnectorMock);
    }

    @Test
    void onboarding_nullBillingData() {
        //given
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData(), "setBillingData");
        //when
        Executable executable = () -> institutionService.onboarding(onboardingData);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_BILLING_DATA_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(productsConnectorMock, partyConnectorMock);
    }

    @Test
    void onboarding_nullOrganizationType() {
        //given
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData(), "setInstitutionType");
        BillingData billingData = TestUtils.mockInstance(new BillingData());
        onboardingData.setBillingData(billingData);
        //when
        Executable executable = () -> institutionService.onboarding(onboardingData);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_TYPE_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(productsConnectorMock, partyConnectorMock);
    }

    @Test
    void onboarding_nullProductRoles() {
        // given
        User userInfo = TestUtils.mockInstance(new User(), "setRole");
        userInfo.setRole(PartyRole.MANAGER);
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        BillingData billingData = TestUtils.mockInstance(new BillingData());
        onboardingData.setUsers(List.of(userInfo));
        onboardingData.setBillingData(billingData);
        Product product = TestUtils.mockInstance(new Product(), "setParentId");
        product.setRoleMappings(new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, null);
        }});
        Mockito.when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(product);
        // when
        Executable executable = () -> institutionService.onboarding(onboardingData);
        // then
        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals(String.format(ATLEAST_ONE_PRODUCT_ROLE_REQUIRED, userInfo.getRole()), e.getMessage());
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProduct(onboardingData.getProductId());
        Mockito.verifyNoMoreInteractions(productsConnectorMock);
        Mockito.verifyNoInteractions(partyConnectorMock);
    }


    @Test
    void onboarding_emptyProductRoles() {
        // given
        User userInfo = TestUtils.mockInstance(new User(), "setRole");
        userInfo.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        BillingData billingData = TestUtils.mockInstance(new BillingData());
        onboardingData.setBillingData(billingData);
        onboardingData.setUsers(List.of(userInfo));
        Product productMock = TestUtils.mockInstance(new Product(), "setRoleMappings", "setParentId");
        ProductRoleInfo productRoleInfo1 = TestUtils.mockInstance(new ProductRoleInfo(), 1, "setRoles");
        productRoleInfo1.setRoles(List.of(TestUtils.mockInstance(new ProductRoleInfo.ProductRole(), 1)));
        ProductRoleInfo productRoleInfo2 = TestUtils.mockInstance(new ProductRoleInfo(), 2, "setRoles");
        productRoleInfo2.setRoles(List.of());
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        productMock.setRoleMappings(roleMappings);
        Mockito.when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(productMock);
        // when
        Executable executable = () -> institutionService.onboarding(onboardingData);
        // then
        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals(String.format(ATLEAST_ONE_PRODUCT_ROLE_REQUIRED, userInfo.getRole()), e.getMessage());
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProduct(onboardingData.getProductId());
        Mockito.verifyNoMoreInteractions(productsConnectorMock);
        Mockito.verifyNoInteractions(partyConnectorMock);
    }

    @Test
    void onboarding_MoreThanOneProductRoles() {
        // given
        User userInfo = TestUtils.mockInstance(new User(), "setRole");
        userInfo.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        BillingData billingData = TestUtils.mockInstance(new BillingData());
        onboardingData.setBillingData(billingData);
        onboardingData.setUsers(List.of(userInfo));
        Product productMock = TestUtils.mockInstance(new Product(), "setRoleMappings", "setParentId");
        ProductRoleInfo productRoleInfo1 = TestUtils.mockInstance(new ProductRoleInfo(), 1, "setRoles");
        productRoleInfo1.setRoles(List.of(TestUtils.mockInstance(new ProductRoleInfo.ProductRole(), 1)));
        ProductRoleInfo productRoleInfo2 = TestUtils.mockInstance(new ProductRoleInfo(), 2, "setRoles");
        productRoleInfo2.setRoles(List.of(TestUtils.mockInstance(new ProductRoleInfo.ProductRole(), 1),
                TestUtils.mockInstance(new ProductRoleInfo.ProductRole(), 2)));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        productMock.setRoleMappings(roleMappings);
        Mockito.when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(productMock);
        // when
        Executable executable = () -> institutionService.onboarding(onboardingData);
        // then
        IllegalStateException e = Assertions.assertThrows(IllegalStateException.class, executable);
        Assertions.assertEquals(String.format(MORE_THAN_ONE_PRODUCT_ROLE_AVAILABLE, userInfo.getRole()), e.getMessage());
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProduct(onboardingData.getProductId());
        Mockito.verifyNoMoreInteractions(productsConnectorMock);
        Mockito.verifyNoInteractions(partyConnectorMock);
    }


    @Test
    void onboarding() {
        // given
        String productRole = "role";
        User userInfo1 = TestUtils.mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = TestUtils.mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        BillingData billingData = TestUtils.mockInstance(new BillingData());
        onboardingData.setBillingData(billingData);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));
        Product productMock = TestUtils.mockInstance(new Product(), "setRoleMappings", "setParentId");
        ProductRoleInfo productRoleInfo1 = TestUtils.mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = TestUtils.mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = TestUtils.mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = TestUtils.mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        productMock.setRoleMappings(roleMappings);
        Mockito.when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(productMock);

        RelationshipInfo relationshipInfoMock = TestUtils.mockInstance(new RelationshipInfo());
        RelationshipsResponse relationshipsResponse = new RelationshipsResponse();
        relationshipsResponse.add(relationshipInfoMock);
        // when
        institutionService.onboarding(onboardingData);
        // then
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProduct(onboardingData.getProductId());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .onboardingOrganization(onboardingDataCaptor.capture());
        OnboardingData captured = onboardingDataCaptor.getValue();
        Assertions.assertNotNull(captured.getUsers());
        Assertions.assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> Assertions.assertEquals(productRole, userInfo.getProductRole()));
        Mockito.verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock);
    }

    @Test
    void onboarding_noRelationshipForSubProduct() {
        //given
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        BillingData billingData = TestUtils.mockInstance(new BillingData());
        onboardingData.setBillingData(billingData);
        Product product = TestUtils.mockInstance(new Product());
        Mockito.when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(product);
        //when
        Executable executable = () -> institutionService.onboarding(onboardingData);
        //then
        ProductHasNoRelationshipException e = Assertions.assertThrows(ProductHasNoRelationshipException.class, executable);
        Assertions.assertEquals(String.format("No relationship for %s and %s", product.getParentId(), onboardingData.getInstitutionId())
                , e.getMessage());
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProduct(onboardingData.getProductId());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUserInstitutionRelationships(onboardingData.getInstitutionId(), product.getParentId());
        Mockito.verifyNoMoreInteractions(partyConnectorMock, productsConnectorMock);
    }

    @Test
    void onboardingSubProduct_noManagerProvided() {
        //given
        User userInfo2 = TestUtils.mockInstance(new User(), 1, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        BillingData billingData = TestUtils.mockInstance(new BillingData());
        onboardingData.setBillingData(billingData);
        onboardingData.setUsers(List.of(userInfo2));
        Product productMock = TestUtils.mockInstance(new Product(), "setRoleMappings");
        Mockito.when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(productMock);
        RelationshipInfo relationshipInfoMock = TestUtils.mockInstance(new RelationshipInfo());
        relationshipInfoMock.setTaxCode(userInfo2.getTaxCode());
        RelationshipsResponse relationshipsResponse = new RelationshipsResponse();
        relationshipsResponse.add(relationshipInfoMock);
        Mockito.when(partyConnectorMock.getUserInstitutionRelationships(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(relationshipsResponse);
        //when
        Executable executable = () -> institutionService.onboarding(onboardingData);
        //then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals(ILLEGAL_LIST_OF_USERS, e.getMessage());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUserInstitutionRelationships(onboardingData.getInstitutionId(), productMock.getParentId());
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProduct(onboardingData.getProductId());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUserInstitutionRelationships(onboardingData.getInstitutionId(), productMock.getParentId());
        Mockito.verifyNoMoreInteractions(partyConnectorMock, productsConnectorMock);


    }

    @Test
    void onboardingSubProduct_invalidManagerForProvidedProduct() {
        //given
        User userInfo1 = TestUtils.mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = TestUtils.mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        BillingData billingData = TestUtils.mockInstance(new BillingData());
        onboardingData.setBillingData(billingData);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));
        Product productMock = TestUtils.mockInstance(new Product(), "setRoleMappings");
        Mockito.when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(productMock);
        RelationshipInfo relationshipInfoMock = TestUtils.mockInstance(new RelationshipInfo());
        relationshipInfoMock.setTaxCode(userInfo2.getTaxCode());
        RelationshipsResponse relationshipsResponse = new RelationshipsResponse();
        relationshipsResponse.add(relationshipInfoMock);
        Mockito.when(partyConnectorMock.getUserInstitutionRelationships(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(relationshipsResponse);
        //when
        Executable executable = () -> institutionService.onboarding(onboardingData);
        //then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals("The provided Manager is not valid for this product", e.getMessage());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUserInstitutionRelationships(onboardingData.getInstitutionId(), productMock.getParentId());
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProduct(onboardingData.getProductId());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUserInstitutionRelationships(onboardingData.getInstitutionId(), productMock.getParentId());
        Mockito.verifyNoMoreInteractions(partyConnectorMock, productsConnectorMock);

    }

    @Test
    void onboardingSubProduct_internalError() {
        //given
        User userInfo1 = TestUtils.mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = TestUtils.mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        BillingData billingData = TestUtils.mockInstance(new BillingData());
        onboardingData.setBillingData(billingData);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));
        Product productMock = TestUtils.mockInstance(new Product(), "setRoleMappings");
        Mockito.when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(productMock);
        RelationshipInfo relationshipInfoMock = TestUtils.mockInstance(new RelationshipInfo());
        relationshipInfoMock.setTaxCode(userInfo2.getTaxCode());
        relationshipInfoMock.setRole(PartyRole.DELEGATE);
        RelationshipsResponse relationshipsResponse = new RelationshipsResponse();
        relationshipsResponse.add(relationshipInfoMock);
        Mockito.when(partyConnectorMock.getUserInstitutionRelationships(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(relationshipsResponse);
        //when
        Executable executable = () -> institutionService.onboarding(onboardingData);
        //then
        InternalServerException e = assertThrows(InternalServerException.class, executable);
        assertEquals("Internal Error: Legal referent not Manager", e.getMessage());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUserInstitutionRelationships(onboardingData.getInstitutionId(), productMock.getParentId());
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProduct(onboardingData.getProductId());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUserInstitutionRelationships(onboardingData.getInstitutionId(), productMock.getParentId());
        Mockito.verifyNoMoreInteractions(partyConnectorMock, productsConnectorMock);

    }

    @Test
    void onboardingSubProduct() {
        // given
        String productRole = "role";
        User userInfo1 = TestUtils.mockInstance(new User(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        User userInfo2 = TestUtils.mockInstance(new User(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        BillingData billingData = TestUtils.mockInstance(new BillingData());
        onboardingData.setBillingData(billingData);
        onboardingData.setUsers(List.of(userInfo1, userInfo2));
        Product productMock = TestUtils.mockInstance(new Product(), "setRoleMappings");
        ProductRoleInfo productRoleInfo1 = TestUtils.mockInstance(new ProductRoleInfo(), 1, "setRoles");
        ProductRoleInfo.ProductRole productRole1 = TestUtils.mockInstance(new ProductRoleInfo.ProductRole(), 1);
        productRole1.setCode(productRole);
        productRoleInfo1.setRoles(List.of(productRole1));
        ProductRoleInfo productRoleInfo2 = TestUtils.mockInstance(new ProductRoleInfo(), 2, "setRoles");
        ProductRoleInfo.ProductRole productRole2 = TestUtils.mockInstance(new ProductRoleInfo.ProductRole(), 2);
        productRole2.setCode(productRole);
        productRoleInfo2.setRoles(List.of(productRole2));
        EnumMap<PartyRole, ProductRoleInfo> roleMappings = new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, productRoleInfo1);
            put(PartyRole.DELEGATE, productRoleInfo2);
        }};
        Mockito.when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(productMock);
        Product productMock2 = TestUtils.mockInstance(new Product());
        productMock2.setParentId(productMock2.getParentId());
        productMock2.setRoleMappings(roleMappings);
        Mockito.when(productsConnectorMock.getProduct(productMock.getParentId()))
                .thenReturn(productMock2);
        RelationshipInfo relationshipInfoMock = TestUtils.mockInstance(new RelationshipInfo());
        relationshipInfoMock.setTaxCode(userInfo1.getTaxCode());
        RelationshipsResponse relationshipsResponse = new RelationshipsResponse();
        relationshipsResponse.add(relationshipInfoMock);
        Mockito.when(partyConnectorMock.getUserInstitutionRelationships(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(relationshipsResponse);
        // when
        institutionService.onboarding(onboardingData);
        // then
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProduct(onboardingData.getProductId());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUserInstitutionRelationships(onboardingData.getInstitutionId(), productMock.getParentId());
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProduct(productMock.getParentId());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .onboardingOrganization(onboardingDataCaptor.capture());
        OnboardingData captured = onboardingDataCaptor.getValue();
        Assertions.assertNotNull(captured.getUsers());
        Assertions.assertEquals(2, captured.getUsers().size());
        captured.getUsers().forEach(userInfo -> Assertions.assertEquals(productRole, userInfo.getProductRole()));
        Mockito.verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock);
    }

    @Test
    void getInstitutions() {
        //given
        InstitutionInfo expectedInstitutionInfo = new InstitutionInfo();
        Mockito.when(partyConnectorMock.getOnBoardedInstitutions())
                .thenReturn(List.of(expectedInstitutionInfo));
        // when
        Collection<InstitutionInfo> institutions = institutionService.getInstitutions();
        // then
        assertNotNull(institutions);
        assertEquals(1, institutions.size());
        assertSame(expectedInstitutionInfo, institutions.iterator().next());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getOnBoardedInstitutions();
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
    }

    @Test
    void getInstitutions_emptyResult() {
        //given
        //when
        Collection<InstitutionInfo> institutions = institutionService.getInstitutions();
        // then
        assertNotNull(institutions);
        assertTrue(institutions.isEmpty());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getOnBoardedInstitutions();
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
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
        Mockito.verifyNoInteractions(partyConnectorMock);
    }

    @Test
    void getInstitutionOnboardingData_noAuthentication() {
        //String
        String institutionId = "institutionId";
        String productId = "productId";
        //when
        Executable executable = () -> institutionService.getInstitutionOnboardingData(institutionId, productId);
        //then
        IllegalStateException e = assertThrows(IllegalStateException.class, executable);
        assertEquals("Authentication is required", e.getMessage());
        Mockito.verifyNoInteractions(partyConnectorMock);
    }

    @Test
    void getInstitutionOnboardingData_nullPrincipal() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        TestSecurityContextHolder.setAuthentication(new TestingAuthenticationToken(null, null));
        //when
        Executable executable = () -> institutionService.getInstitutionOnboardingData(institutionId, productId);
        //then
        IllegalStateException e = assertThrows(IllegalStateException.class, executable);
        assertEquals("Not SelfCareUser principal", e.getMessage());
        Mockito.verifyNoInteractions(partyConnectorMock);
    }

    @Test
    void getInstitutionOnboardingData_notAdmin() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        String loggedUser = "loggedUser";
        SelfCareUser selfCareUser = SelfCareUser.builder(loggedUser).email("test@example.com").build();
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(selfCareUser, null);
        TestSecurityContextHolder.setAuthentication(authenticationToken);
        UserInfo userInfoMock = TestUtils.mockInstance(new UserInfo(), "setId", "setRole");
        userInfoMock.setId(loggedUser);
        Mockito.when(partyConnectorMock.getUsers(Mockito.anyString(), Mockito.any()))
                .thenReturn(Collections.emptyList());
        InstitutionInfo institutionInfoMock = TestUtils.mockInstance(new InstitutionInfo());
        BillingData billingDataMock = TestUtils.mockInstance(new BillingData());
        institutionInfoMock.setBilling(billingDataMock);
        Mockito.when(partyConnectorMock.getOnboardedInstitution(Mockito.anyString()))
                .thenReturn(institutionInfoMock);
        //when
        InstitutionOnboardingData institutionOnboardingData = institutionService.getInstitutionOnboardingData(institutionId, productId);
        //then
        assertNotNull(institutionOnboardingData);
        assertNull(institutionOnboardingData.getManager());
        assertNotNull(institutionOnboardingData.getInstitution());
        assertEquals(institutionInfoMock.getInstitutionId(), institutionOnboardingData.getInstitution().getInstitutionId());
        assertEquals(institutionInfoMock.getAddress(), institutionOnboardingData.getInstitution().getAddress());
        assertEquals(institutionInfoMock.getInstitutionType(), institutionOnboardingData.getInstitution().getInstitutionType());
        assertEquals(institutionInfoMock.getCategory(), institutionOnboardingData.getInstitution().getCategory());
        assertEquals(institutionInfoMock.getDescription(), institutionOnboardingData.getInstitution().getDescription());
        assertEquals(institutionInfoMock.getDescription(), institutionOnboardingData.getInstitution().getDescription());
        assertEquals(institutionInfoMock.getTaxCode(), institutionOnboardingData.getInstitution().getTaxCode());
        assertEquals(institutionInfoMock.getZipCode(), institutionOnboardingData.getInstitution().getZipCode());
        assertEquals(institutionInfoMock.getDigitalAddress(), institutionOnboardingData.getInstitution().getDigitalAddress());
        assertEquals(institutionInfoMock.getBilling().getVatNumber(), institutionOnboardingData.getInstitution().getBilling().getVatNumber());
        assertEquals(institutionInfoMock.getBilling().getRecipientCode(), institutionOnboardingData.getInstitution().getBilling().getRecipientCode());
        assertEquals(institutionInfoMock.getBilling().getPublicServices(), institutionOnboardingData.getInstitution().getBilling().getPublicServices());
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUsers(Mockito.eq(institutionId), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertEquals(capturedFilter.getAllowedStates().get(), EnumSet.of(ACTIVE));
        assertEquals(capturedFilter.getRole().get(), EnumSet.of(PartyRole.MANAGER, PartyRole.DELEGATE, PartyRole.SUB_DELEGATE));
        assertEquals(capturedFilter.getProductId().get(), productId);
        assertEquals(capturedFilter.getUserId().get(), loggedUser);
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getOnboardedInstitution(institutionId);
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
    }

    @Test
    void getInstitutionOnboardingData_ManagerNotFound() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        String loggedUser = "loggedUser";
        EnumSet<PartyRole> roles = Arrays.stream(PartyRole.values())
                .filter(partyRole -> SelfCareAuthority.ADMIN.equals(partyRole.getSelfCareAuthority()))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(PartyRole.class)));
        SelfCareUser selfCareUser = SelfCareUser.builder(loggedUser).email("test@example.com").build();
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(selfCareUser, null);
        TestSecurityContextHolder.setAuthentication(authenticationToken);
        UserInfo userInfoMock = TestUtils.mockInstance(new UserInfo(), "setId", "setRole");
        userInfoMock.setId(loggedUser);
        UserInfo.UserInfoFilter filterLoggedUsers = new UserInfo.UserInfoFilter();
        filterLoggedUsers.setRole(Optional.of(EnumSet.of(PartyRole.DELEGATE)));
        Mockito.when(partyConnectorMock.getUsers(Mockito.anyString(), Mockito.any()))
                .thenAnswer(invocation -> {
                            UserInfo.UserInfoFilter argument = invocation.getArgument(1, UserInfo.UserInfoFilter.class);
                            if (argument.getUserId().isPresent())
                                return Collections.singletonList(userInfoMock);
                            else
                                return Collections.emptyList();
                        }
                );
        //when
        Executable executable = () -> institutionService.getInstitutionOnboardingData(institutionId, productId);
        //then
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, executable);
        assertEquals(String.format("No Manager found for given institution: %s", institutionId), e.getMessage());

        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        Mockito.verify(partyConnectorMock, Mockito.times(2))
                .getUsers(Mockito.eq(institutionId), filterCaptor.capture());
        List<UserInfo.UserInfoFilter> capturedFilters = filterCaptor.getAllValues();

        assertEquals(roles, capturedFilters.get(0).getRole().get());
        assertEquals(EnumSet.of(ACTIVE), capturedFilters.get(0).getAllowedStates().get());
        assertEquals(loggedUser, capturedFilters.get(0).getUserId().get());
        assertEquals(productId, capturedFilters.get(0).getProductId().get());
        assertTrue(capturedFilters.get(0).getProductRoles().isEmpty());

        assertEquals(EnumSet.of(PartyRole.MANAGER), capturedFilters.get(1).getRole().get());
        assertEquals(productId, capturedFilters.get(1).getProductId().get());
        assertTrue(capturedFilters.get(1).getProductRoles().isEmpty());
        assertTrue(capturedFilters.get(1).getUserId().isEmpty());
        assertEquals(EnumSet.of(ACTIVE), capturedFilters.get(1).getAllowedStates().get());

        Mockito.verify(partyConnectorMock, Mockito.times(0))
                .getOnboardedInstitution(institutionId);
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
    }

    @Test
    void getInstitutionOnboardingData_institutionNotFound() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        String loggedUser = "loggedUser";
        SelfCareUser selfCareUser = SelfCareUser.builder(loggedUser).email("test@example.com").build();
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(selfCareUser, null);
        TestSecurityContextHolder.setAuthentication(authenticationToken);
        UserInfo userInfoMock = TestUtils.mockInstance(new UserInfo(), "setId", "setRole");
        userInfoMock.setId(loggedUser);
        Mockito.when(partyConnectorMock.getUsers(Mockito.anyString(), Mockito.any()))
                .thenReturn(Collections.emptyList());
        //when
        Executable executable = () -> institutionService.getInstitutionOnboardingData(institutionId, productId);
        //then
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, executable);
        assertEquals(String.format("Institution %s not found", institutionId), e.getMessage());
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUsers(Mockito.eq(institutionId), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertEquals(capturedFilter.getAllowedStates().get(), EnumSet.of(ACTIVE));
        assertEquals(capturedFilter.getRole().get(), EnumSet.of(PartyRole.MANAGER, PartyRole.DELEGATE, PartyRole.SUB_DELEGATE));
        assertEquals(capturedFilter.getProductId().get(), productId);
        assertEquals(capturedFilter.getUserId().get(), loggedUser);
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getOnboardedInstitution(institutionId);
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
    }


    @Test
    void getInstitutionOnboardingData() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        String loggedUser = "loggedUser";
        EnumSet<PartyRole> roles = Arrays.stream(PartyRole.values())
                .filter(partyRole -> SelfCareAuthority.ADMIN.equals(partyRole.getSelfCareAuthority()))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(PartyRole.class)));
        SelfCareUser selfCareUser = SelfCareUser.builder(loggedUser).email("test@example.com").build();
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(selfCareUser, null);
        TestSecurityContextHolder.setAuthentication(authenticationToken);
        UserInfo userInfoMock = TestUtils.mockInstance(new UserInfo(), "setId", "setRole");
        userInfoMock.setId(loggedUser);
        UserInfo userInfoManager = TestUtils.mockInstance(new UserInfo());
        userInfoManager.setRole(PartyRole.MANAGER);
        Mockito.when(partyConnectorMock.getUsers(Mockito.anyString(), Mockito.any()))
                .thenAnswer(invocation -> {
                            UserInfo.UserInfoFilter argument = invocation.getArgument(1, UserInfo.UserInfoFilter.class);
                            if (argument.getUserId().isPresent())
                                return Collections.singletonList(userInfoMock);
                            else
                                return Collections.singleton(userInfoManager);
                        }
                );
        InstitutionInfo institutionInfoMock = TestUtils.mockInstance(new InstitutionInfo());
        BillingData billingDataMock = TestUtils.mockInstance(new BillingData());
        institutionInfoMock.setBilling(billingDataMock);
        Mockito.when(partyConnectorMock.getOnboardedInstitution(Mockito.anyString()))
                .thenReturn(institutionInfoMock);
        //when
        InstitutionOnboardingData institutionOnboardingData = institutionService.getInstitutionOnboardingData(institutionId, productId);
        //then
        assertNotNull(institutionOnboardingData);
        assertNotNull(institutionOnboardingData.getManager());
        assertEquals(userInfoManager, institutionOnboardingData.getManager());
        assertNotNull(institutionOnboardingData.getInstitution());
        assertEquals(institutionInfoMock, institutionOnboardingData.getInstitution());

        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        Mockito.verify(partyConnectorMock, Mockito.times(2))
                .getUsers(Mockito.eq(institutionId), filterCaptor.capture());
        List<UserInfo.UserInfoFilter> capturedFilters = filterCaptor.getAllValues();

        assertEquals(roles, capturedFilters.get(0).getRole().get());
        assertEquals(EnumSet.of(ACTIVE), capturedFilters.get(0).getAllowedStates().get());
        assertEquals(loggedUser, capturedFilters.get(0).getUserId().get());
        assertEquals(productId, capturedFilters.get(0).getProductId().get());
        assertTrue(capturedFilters.get(0).getProductRoles().isEmpty());

        assertEquals(EnumSet.of(PartyRole.MANAGER), capturedFilters.get(1).getRole().get());
        assertEquals(productId, capturedFilters.get(1).getProductId().get());
        assertTrue(capturedFilters.get(1).getProductRoles().isEmpty());
        assertTrue(capturedFilters.get(1).getUserId().isEmpty());
        assertEquals(EnumSet.of(ACTIVE), capturedFilters.get(1).getAllowedStates().get());

        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getOnboardedInstitution(institutionId);
        Mockito.verifyNoMoreInteractions(partyConnectorMock);

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
        Mockito.verifyNoInteractions(partyConnectorMock);
    }

    @Test
    void getInstitutionByExternalId() {
        //given
        String institutionId = "institutionId";
        Institution institutionMock = TestUtils.mockInstance(new Institution());
        Attribute attribute = TestUtils.mockInstance(new Attribute());
        institutionMock.setAttributes(List.of(attribute));
        Mockito.when(partyConnectorMock.getInstitutionByExternalId(Mockito.anyString()))
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
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getInstitutionByExternalId(institutionId);
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
    }

}