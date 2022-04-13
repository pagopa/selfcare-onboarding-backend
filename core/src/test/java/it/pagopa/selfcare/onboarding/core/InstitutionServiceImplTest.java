package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.api.PartyConnector;
import it.pagopa.selfcare.onboarding.connector.api.ProductsConnector;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipInfo;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipState;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipsResponse;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.*;
import it.pagopa.selfcare.onboarding.connector.model.product.Product;
import it.pagopa.selfcare.onboarding.connector.model.product.ProductRoleInfo;
import it.pagopa.selfcare.onboarding.core.exceptions.InternalServerException;
import it.pagopa.selfcare.onboarding.core.exceptions.ProductHasNoRelationshipException;
import it.pagopa.selfcare.onboarding.core.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ValidationException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

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


    //TODO add test for institutionType
    @Test
    void onboarding_nullRoleMapping() {
        // given
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        Product product = TestUtils.mockInstance(new Product(), "setParent");
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
        Product product = TestUtils.mockInstance(new Product(), "setParent");
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
        Product productMock = TestUtils.mockInstance(new Product(), "setRoleMappings", "setParent");
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
        Product productMock = TestUtils.mockInstance(new Product(), "setRoleMappings", "setParent");
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
        Product productMock = TestUtils.mockInstance(new Product(), "setRoleMappings", "setParent");
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
        Assertions.assertEquals(String.format("No relationship for %s and %s", product.getParent(), onboardingData.getInstitutionId())
                , e.getMessage());
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProduct(onboardingData.getProductId());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUserInstitutionRelationships(onboardingData.getInstitutionId(), product.getParent());
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
                .getUserInstitutionRelationships(onboardingData.getInstitutionId(), productMock.getParent());
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProduct(onboardingData.getProductId());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUserInstitutionRelationships(onboardingData.getInstitutionId(), productMock.getParent());
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
                .getUserInstitutionRelationships(onboardingData.getInstitutionId(), productMock.getParent());
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProduct(onboardingData.getProductId());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUserInstitutionRelationships(onboardingData.getInstitutionId(), productMock.getParent());
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
                .getUserInstitutionRelationships(onboardingData.getInstitutionId(), productMock.getParent());
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProduct(onboardingData.getProductId());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUserInstitutionRelationships(onboardingData.getInstitutionId(), productMock.getParent());
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
        productMock2.setParent(productMock2.getParent());
        productMock2.setRoleMappings(roleMappings);
        Mockito.when(productsConnectorMock.getProduct(productMock.getParent()))
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
                .getUserInstitutionRelationships(onboardingData.getInstitutionId(), productMock.getParent());
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProduct(productMock.getParent());
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
    void getManager_nullInstitutionId() {
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
    void getManager_emptyResult() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        //when
        Executable executable = () -> institutionService.getInstitutionOnboardingData(institutionId, productId);
        //then
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, executable);
        assertEquals("No Manager found for given institution", e.getMessage());
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUsers(Mockito.eq(institutionId), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertEquals(capturedFilter.getAllowedStates().get(), EnumSet.of(RelationshipState.ACTIVE));
        assertEquals(capturedFilter.getRole().get(), PartyRole.MANAGER);
        assertEquals(capturedFilter.getProductId().get(), productId);
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
    }

    @Test
    void getInstitutionOnboardingData() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        UserInfo userInfoMock = TestUtils.mockInstance(new UserInfo(), "setRole");
        userInfoMock.setRole(PartyRole.MANAGER);
        Mockito.when(partyConnectorMock.getUsers(Mockito.anyString(), Mockito.any()))
                .thenReturn(List.of(userInfoMock));
        //when
        InstitutionOnboardingData institutionOnboardingData = institutionService.getInstitutionOnboardingData(institutionId, productId);
        //then
        assertNotNull(institutionOnboardingData);
        assertEquals(userInfoMock.getInstitutionId(), institutionOnboardingData.getManager().getInstitutionId());
        assertEquals(userInfoMock.getId(), institutionOnboardingData.getManager().getId());
        assertEquals(userInfoMock.getEmail(), institutionOnboardingData.getManager().getEmail());
        assertEquals(userInfoMock.getRole(), institutionOnboardingData.getManager().getRole());
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUsers(Mockito.eq(institutionId), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertEquals(capturedFilter.getAllowedStates().get(), EnumSet.of(RelationshipState.ACTIVE));
        assertEquals(capturedFilter.getRole().get(), PartyRole.MANAGER);
        assertEquals(capturedFilter.getProductId().get(), productId);
        Mockito.verifyNoMoreInteractions(partyConnectorMock);

    }


}