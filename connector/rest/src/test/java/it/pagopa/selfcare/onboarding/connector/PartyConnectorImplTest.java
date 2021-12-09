package it.pagopa.selfcare.onboarding.connector;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PartyRole;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
import it.pagopa.selfcare.onboarding.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnboardingRequest;
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
class PartyConnectorImplTest {

    @InjectMocks
    private PartyConnectorImpl partyConnector;

    @Mock
    private PartyProcessRestClient restClientMock;

    @Captor
    ArgumentCaptor<OnboardingRequest> onboardingRequestCaptor;


    @Test
    void onboardingOrganization_nullOnboardingData() {
        // given
        OnboardingData onboardingData = null;
        // when
        Executable executable = () -> partyConnector.onboardingOrganization(onboardingData);
        // then
        Assertions.assertThrows(IllegalArgumentException.class, executable);
        Mockito.verifyNoInteractions(restClientMock);
    }


    @Test
    void onboardingOrganization_emptyUsers() {
        // given
        OnboardingData onboardingData =
                new OnboardingData("institutionId", "productId", Collections.emptyList());
        // when
        partyConnector.onboardingOrganization(onboardingData);
        // then
        Mockito.verify(restClientMock, Mockito.times(1))
                .onboardingOrganization(onboardingRequestCaptor.capture());
        OnboardingRequest request = onboardingRequestCaptor.getValue();
        Assertions.assertEquals(onboardingData.getInstitutionId(), request.getInstitutionId());
        Assertions.assertNotNull(request.getUsers());
        Assertions.assertTrue(request.getUsers().isEmpty());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void onboardingOrganization() {
        // given
        OnboardingData onboardingData =
                new OnboardingData("institutionId", "productId", List.of(TestUtils.mockInstance(new DummyUserInfo())));
        // when
        partyConnector.onboardingOrganization(onboardingData);
        // then
        Mockito.verify(restClientMock, Mockito.times(1))
                .onboardingOrganization(onboardingRequestCaptor.capture());
        OnboardingRequest request = onboardingRequestCaptor.getValue();
        Assertions.assertEquals(onboardingData.getInstitutionId(), request.getInstitutionId());
        Assertions.assertNotNull(request.getUsers());
        Assertions.assertEquals(1, request.getUsers().size());
        Assertions.assertEquals(onboardingData.getProductId(), request.getUsers().get(0).getProduct());
        Assertions.assertEquals(onboardingData.getUsers().get(0).getName(), request.getUsers().get(0).getName());
        Assertions.assertEquals(onboardingData.getUsers().get(0).getSurname(), request.getUsers().get(0).getSurname());
        Assertions.assertEquals(onboardingData.getUsers().get(0).getTaxCode(), request.getUsers().get(0).getTaxCode());
        Assertions.assertEquals(onboardingData.getUsers().get(0).getRole(), request.getUsers().get(0).getRole());
        Assertions.assertEquals(onboardingData.getUsers().get(0).getEmail(), request.getUsers().get(0).getEmail());
        Assertions.assertEquals(onboardingData.getUsers().get(0).getProductRole(), request.getUsers().get(0).getProductRole());
        Mockito.verifyNoMoreInteractions(restClientMock);
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