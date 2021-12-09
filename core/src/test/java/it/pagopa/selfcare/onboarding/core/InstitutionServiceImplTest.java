package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.api.PartyConnector;
import it.pagopa.selfcare.onboarding.connector.api.ProductsConnector;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PartyRole;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
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
        Assertions.assertThrows(IllegalArgumentException.class, executable);
        Mockito.verifyNoInteractions(partyConnectorMock);
    }


    @Test
    void onboarding_emptyUsers() {
        // given
        OnboardingData onboardingData =
                new OnboardingData("institutionId", "productId", Collections.emptyList());
        // when
        institutionService.onboarding(onboardingData);
        // then
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .onboardingOrganization(onboardingDataCaptor.capture());
        OnboardingData captured = onboardingDataCaptor.getValue();
        Assertions.assertNotNull(captured.getUsers());
        Assertions.assertTrue(captured.getUsers().isEmpty());
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
    }


    @Test
    void onboarding() {
        // given
        OnboardingData onboardingData =
                new OnboardingData("institutionId", "productId", List.of(TestUtils.mockInstance(new DummyUserInfo())));
        // when
        institutionService.onboarding(onboardingData);
        // then
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .onboardingOrganization(onboardingDataCaptor.capture());
        OnboardingData captured = onboardingDataCaptor.getValue();
        Assertions.assertNotNull(captured.getUsers());
        Assertions.assertEquals(1, captured.getUsers().size());
        Assertions.assertEquals(null, captured.getUsers().get(0).getProductRole());//FIXME
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