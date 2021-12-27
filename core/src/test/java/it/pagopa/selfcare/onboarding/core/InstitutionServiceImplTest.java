package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.api.PartyConnector;
import it.pagopa.selfcare.onboarding.connector.api.ProductsConnector;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PartyRole;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
import it.pagopa.selfcare.onboarding.connector.model.product.Product;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
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
        Product product = TestUtils.mockInstance(new Product());
        product.setRoleMappings(new EnumMap<>(PartyRole.class) {{
            put(PartyRole.DELEGATE, Collections.emptyList());
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
    void onboarding() {
        // given
        DummyUserInfo userInfo = TestUtils.mockInstance(new DummyUserInfo(), "setRole");
        userInfo.setRole(PartyRole.MANAGER);
        String productRole = "role";
        OnboardingData onboardingData =
                new OnboardingData("institutionId", "productId", List.of(userInfo));
        Product product = TestUtils.mockInstance(new Product());
        product.setRoleMappings(new EnumMap<>(PartyRole.class) {{
            put(PartyRole.MANAGER, List.of(productRole));
        }});
        Mockito.when(productsConnectorMock.getProduct(onboardingData.getProductId()))
                .thenReturn(product);
        // when
        institutionService.onboarding(onboardingData);
        // then
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProduct(onboardingData.getProductId());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .onboardingOrganization(onboardingDataCaptor.capture());
        OnboardingData captured = onboardingDataCaptor.getValue();
        Assertions.assertNotNull(captured.getUsers());
        Assertions.assertEquals(1, captured.getUsers().size());
        Assertions.assertEquals(productRole, captured.getUsers().get(0).getProductRole());
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