package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.api.PartyConnector;
import it.pagopa.selfcare.onboarding.connector.api.ProductsConnector;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipInfo;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipsResponse;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PartyRole;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
import it.pagopa.selfcare.onboarding.connector.model.product.Product;
import it.pagopa.selfcare.onboarding.connector.model.product.ProductRoleInfo;
import it.pagopa.selfcare.onboarding.core.exceptions.ProductHasNoRelationshipException;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

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
        Assertions.assertEquals("Onboarding data is required", e.getMessage());
        Mockito.verifyNoInteractions(partyConnectorMock, productsConnectorMock);
    }

//    @Test
//    void onboarding_nullInstitutionId() {
//        // given
//        String institutionId = null;
//        String productId = "productId";
//        List<UserInfo> users = List.of(Mockito.mock(UserInfo.class));
//        // when
//        Executable executable = () -> institutionService.onboarding(institutionId, productId, users);
//        // then
//        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, executable);
//        Assertions.assertEquals("InstitutionId id is required", e.getMessage());
//        Mockito.verifyNoInteractions(partyConnectorMock, productsConnectorMock);
//    }
//
//    @Test
//    void onboarding_nullProductId() {
//        // given
//        String institutionId = "institutionId";
//        String productId = null;
//        List<UserInfo> users = List.of(TestUtils.mockInstance(new DummyUserInfo()));
//        // when
//        Executable executable = () -> institutionService.onboarding(institutionId, productId, users);
//        // then
//        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, executable);
//        Assertions.assertEquals("Product id is required", e.getMessage());
//        Mockito.verifyNoInteractions(partyConnectorMock, productsConnectorMock);
//    }
//
//
//    @Test
//    void onboarding_emptyUsers() {
//        // given
//        String institutionId = "institutionId";
//        String productId = "productId";
//        List<UserInfo> users = Collections.emptyList();
//        // when
//        Executable executable = () -> institutionService.onboarding(institutionId, productId, users);
//        // then
//        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, executable);
//        Assertions.assertEquals("At least one user is required", e.getMessage());
//        Mockito.verifyNoInteractions(partyConnectorMock, productsConnectorMock);
//    }


    @Test
    void onboarding_nullRoleMapping() {
        // given
        OnboardingData onboardingData =
                new OnboardingData("institutionId", "productId", List.of(TestUtils.mockInstance(new DummyUserInfo())), null);
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
    void onboarding_nullProductRoles() {
        // given
        DummyUserInfo userInfo = TestUtils.mockInstance(new DummyUserInfo(), "setRole");
        userInfo.setRole(PartyRole.MANAGER);
        OnboardingData onboardingData =
                new OnboardingData("institutionId", "productId", List.of(userInfo), null);
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
        Assertions.assertEquals("At least one Product role related to " + userInfo.getRole() + " Party role is required", e.getMessage());
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProduct(onboardingData.getProductId());
        Mockito.verifyNoMoreInteractions(productsConnectorMock);
        Mockito.verifyNoInteractions(partyConnectorMock);
    }


    @Test
    void onboarding_emptyProductRoles() {
        // given
        DummyUserInfo userInfo = TestUtils.mockInstance(new DummyUserInfo(), "setRole");
        userInfo.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData =
                new OnboardingData("institutionId", "productId", List.of(userInfo), null);
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
        Assertions.assertEquals("At least one Product role related to " + userInfo.getRole() + " Party role is required", e.getMessage());
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProduct(onboardingData.getProductId());
        Mockito.verifyNoMoreInteractions(productsConnectorMock);
        Mockito.verifyNoInteractions(partyConnectorMock);
    }

    @Test
    void onboarding_MoreThanOneProductRoles() {
        // given
        DummyUserInfo userInfo = TestUtils.mockInstance(new DummyUserInfo(), "setRole");
        userInfo.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData =
                new OnboardingData("institutionId", "productId", List.of(userInfo), null);
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
        Assertions.assertEquals("More than one Product role related to " + userInfo.getRole() + " Party role is available. Cannot automatically set the Product role", e.getMessage());
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProduct(onboardingData.getProductId());
        Mockito.verifyNoMoreInteractions(productsConnectorMock);
        Mockito.verifyNoInteractions(partyConnectorMock);
    }


    @Test
    void onboarding() {
        // given
        String productRole = "role";
        DummyUserInfo userInfo1 = TestUtils.mockInstance(new DummyUserInfo(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        DummyUserInfo userInfo2 = TestUtils.mockInstance(new DummyUserInfo(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData =
                new OnboardingData("institutionId", "productId", List.of(userInfo1, userInfo2), null);
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
        OnboardingData onboardingData =
                new OnboardingData("institutionId", "productId", List.of(TestUtils.mockInstance(new DummyUserInfo())), null);
        Product product = TestUtils.mockInstance(new Product());
        Mockito.when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(product);
        //when
        Executable executable = () -> institutionService.onboarding(onboardingData);
        //then
        ProductHasNoRelationshipException e = Assertions.assertThrows(ProductHasNoRelationshipException.class, executable);
        Assertions.assertEquals("No relationship for "
                        + product.getParent()
                        + " and "
                        + onboardingData.getInstitutionId()
                , e.getMessage());
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProduct(onboardingData.getProductId());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUserInstitutionRelationships(onboardingData.getInstitutionId(), product.getParent());
        Mockito.verifyNoMoreInteractions(partyConnectorMock, productsConnectorMock);
    }

    @Test
    void onboardingSubProduct() {
        // given
        String productRole = "role";
        DummyUserInfo userInfo1 = TestUtils.mockInstance(new DummyUserInfo(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        DummyUserInfo userInfo2 = TestUtils.mockInstance(new DummyUserInfo(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData =
                new OnboardingData("institutionId", "productId", List.of(userInfo1, userInfo2), null);
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

    @Getter
    @Setter
    private static class DummyUserInfo implements UserInfo {
        private String name;
        private String surname;
        private String taxCode;
        private PartyRole role;
        private String email;
        private String productRole;
    }

}