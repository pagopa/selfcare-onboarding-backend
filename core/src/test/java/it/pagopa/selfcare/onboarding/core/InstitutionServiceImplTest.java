package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.api.PartyConnector;
import it.pagopa.selfcare.onboarding.connector.api.ProductsConnector;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PartyRole;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
import it.pagopa.selfcare.onboarding.connector.model.product.Product;
import it.pagopa.selfcare.onboarding.connector.model.product.ProductRoleInfo;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.EnumMap;
import java.util.List;

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
                new OnboardingData("institutionId", "productId", List.of(TestUtils.mockInstance(new DummyUserInfo())));
        Mockito.when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(TestUtils.mockInstance(new Product()));
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
                new OnboardingData("institutionId", "productId", List.of(userInfo));
        Product product = TestUtils.mockInstance(new Product());
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
                new OnboardingData("institutionId", "productId", List.of(userInfo));
        Product productMock = TestUtils.mockInstance(new Product(), "setRoleMappings");
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
    void onboarding() {
        // given
        String productRole = "role";
        DummyUserInfo userInfo1 = TestUtils.mockInstance(new DummyUserInfo(), 1, "setRole");
        userInfo1.setRole(PartyRole.MANAGER);
        DummyUserInfo userInfo2 = TestUtils.mockInstance(new DummyUserInfo(), 2, "setRole");
        userInfo2.setRole(PartyRole.DELEGATE);
        OnboardingData onboardingData =
                new OnboardingData("institutionId", "productId", List.of(userInfo1, userInfo2));
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
        productMock.setRoleMappings(roleMappings);
        Mockito.when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(productMock);
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