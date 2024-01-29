package it.pagopa.selfcare.onboarding.connector;

import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.*;
import it.pagopa.selfcare.onboarding.connector.rest.client.MsOnboardingApiClient;
import it.pagopa.selfcare.onboarding.connector.rest.mapper.OnboardingMapper;
import it.pagopa.selfcare.onboarding.connector.rest.mapper.OnboardingMapperImpl;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OnboardingMsConnectorImplTest {

    @InjectMocks
    private OnboardingMsConnectorImpl onboardingMsConnector;
    @Mock
    private MsOnboardingApiClient msOnboardingApiClient;

    @Spy
    private OnboardingMapper onboardingMapper = new OnboardingMapperImpl();

    @Test
    void onboarding_institutionDefault() {
        // given
        OnboardingData onboardingData = new OnboardingData();
        onboardingData.setTaxCode("taxCode");
        onboardingData.setInstitutionType(InstitutionType.GSP);
        Billing billing = mockInstance(new Billing());
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setTaxCode("taxCode");
        institutionUpdate.setDescription("description");
        onboardingData.setBilling(billing);
        onboardingData.setUsers(List.of(mockInstance(new User())));
        onboardingData.setInstitutionUpdate(institutionUpdate);
        // when
        onboardingMsConnector.onboarding(onboardingData);
        // then

        ArgumentCaptor<OnboardingDefaultRequest> onboardingRequestCaptor = ArgumentCaptor.forClass(OnboardingDefaultRequest.class);
        verify(msOnboardingApiClient, times(1))
                ._v1OnboardingPost(onboardingRequestCaptor.capture());
        OnboardingDefaultRequest actual = onboardingRequestCaptor.getValue();
        assertEquals(actual.getInstitution().getTaxCode(), institutionUpdate.getTaxCode());
        assertEquals(actual.getInstitution().getDescription(), institutionUpdate.getDescription());
        verifyNoMoreInteractions(msOnboardingApiClient);
    }

    @Test
    void onboarding_institutionPa() {
        // given
        OnboardingData onboardingData = new OnboardingData();
        onboardingData.setTaxCode("taxCode");
        onboardingData.setInstitutionType(InstitutionType.PA);
        Billing billing = mockInstance(new Billing());
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setTaxCode("taxCode");
        onboardingData.setBilling(billing);
        onboardingData.setUsers(List.of(mockInstance(new User())));
        onboardingData.setInstitutionUpdate(institutionUpdate);
        // when
        onboardingMsConnector.onboarding(onboardingData);
        // then

        ArgumentCaptor<OnboardingPaRequest> onboardingRequestCaptor = ArgumentCaptor.forClass(OnboardingPaRequest.class);
        verify(msOnboardingApiClient, times(1))
                ._v1OnboardingPaPost(onboardingRequestCaptor.capture());
        OnboardingPaRequest actual = onboardingRequestCaptor.getValue();
        assertEquals(actual.getInstitution().getTaxCode(), institutionUpdate.getTaxCode());
        verifyNoMoreInteractions(msOnboardingApiClient);
    }
    @Test
    void onboarding_institutionPsp() {
        // given
        OnboardingData onboardingData = new OnboardingData();
        onboardingData.setTaxCode("taxCode");
        onboardingData.setInstitutionType(InstitutionType.PSP);
        Billing billing = mockInstance(new Billing());
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setTaxCode("taxCode");
        institutionUpdate.setPaymentServiceProvider(new PaymentServiceProvider());
        institutionUpdate.setDataProtectionOfficer(new DataProtectionOfficer());
        onboardingData.setBilling(billing);
        onboardingData.setUsers(List.of(mockInstance(new User())));
        onboardingData.setInstitutionUpdate(institutionUpdate);
        // when
        onboardingMsConnector.onboarding(onboardingData);
        // then
        ArgumentCaptor<OnboardingPspRequest> onboardingRequestCaptor = ArgumentCaptor.forClass(OnboardingPspRequest.class);
        verify(msOnboardingApiClient, times(1))
                ._v1OnboardingPspPost(onboardingRequestCaptor.capture());
        OnboardingPspRequest actual = onboardingRequestCaptor.getValue();
        assertEquals(actual.getInstitution().getTaxCode(), institutionUpdate.getTaxCode());
        assertNotNull(actual.getInstitution().getPaymentServiceProvider());
        assertNotNull(actual.getInstitution().getDataProtectionOfficer());
        verifyNoMoreInteractions(msOnboardingApiClient);
    }
    @Test
    void onboardingCompany() {
        // given
        OnboardingData onboardingData = new OnboardingData();
        onboardingData.setTaxCode("taxCode");
        onboardingData.setInstitutionType(InstitutionType.PG);
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setTaxCode("taxCode");
        onboardingData.setUsers(List.of(mockInstance(new User())));
        onboardingData.setInstitutionUpdate(institutionUpdate);
        // when
        onboardingMsConnector.onboardingCompany(onboardingData);
        // then
        ArgumentCaptor<OnboardingPgRequest> onboardingRequestCaptor = ArgumentCaptor.forClass(OnboardingPgRequest.class);
        verify(msOnboardingApiClient, times(1))
                ._v1OnboardingPgCompletionPost(onboardingRequestCaptor.capture());
        OnboardingPgRequest actual = onboardingRequestCaptor.getValue();
        assertEquals(actual.getTaxCode(), institutionUpdate.getTaxCode());
        assertEquals(actual.getDigitalAddress(), institutionUpdate.getDigitalAddress());
        verifyNoMoreInteractions(msOnboardingApiClient);
    }

    @Test
    void onboardingComplete() throws IOException {
        // given
        final String tokenId = "tokenId";
        final MockMultipartFile mockMultipartFile =
                new MockMultipartFile("example", new ByteArrayInputStream("example".getBytes(StandardCharsets.UTF_8)));
        // when
        final Executable executable = () -> msOnboardingApiClient._v1OnboardingOnboardingIdCompletePut(tokenId, mockMultipartFile);
        // then
        assertDoesNotThrow(executable);
        verify(msOnboardingApiClient, times(1))
                ._v1OnboardingOnboardingIdCompletePut(tokenId, mockMultipartFile);
        verifyNoMoreInteractions(msOnboardingApiClient);
    }

    @Test
    void onboardingPending() {
        // given
        final String onboardingId = "onboardingId";
        // when
        final Executable executable = () -> onboardingMsConnector.onboardingPending(onboardingId);
        // then
        assertDoesNotThrow(executable);
        verify(msOnboardingApiClient, times(1))
                ._v1OnboardingOnboardingIdPendingGet(onboardingId);
        verifyNoMoreInteractions(msOnboardingApiClient);
    }

    @Test
    void getOnboardingWithUserInfo() {
        // given
        final String onboardingId = "onboardingId";
        when(msOnboardingApiClient._v1OnboardingOnboardingIdWithUserInfoGet(onboardingId))
                .thenReturn(ResponseEntity.of(Optional.of(new OnboardingGet())));
        // when
        final Executable executable = () -> onboardingMsConnector.getOnboardingWithUserInfo(onboardingId);
        // then
        assertDoesNotThrow(executable);
        verify(msOnboardingApiClient, times(1))
                ._v1OnboardingOnboardingIdWithUserInfoGet(onboardingId);
        verifyNoMoreInteractions(msOnboardingApiClient);
    }

    @Test
    void approveOnboarding() {
        // given
        final String onboardingId = "onboardingId";
        // when
        final Executable executable = () -> onboardingMsConnector.approveOnboarding(onboardingId);
        // then
        assertDoesNotThrow(executable);
        verify(msOnboardingApiClient, times(1))
                ._v1OnboardingOnboardingIdApprovePut(onboardingId);
        verifyNoMoreInteractions(msOnboardingApiClient);
    }

    @Test
    void rejectOnboarding() {
        // given
        final String onboardingId = "onboardingId";
        // when
        final Executable executable = () -> onboardingMsConnector.rejectOnboarding(onboardingId);
        // then
        assertDoesNotThrow(executable);
        verify(msOnboardingApiClient, times(1))
                ._v1OnboardingOnboardingIdRejectPut(onboardingId);
        verifyNoMoreInteractions(msOnboardingApiClient);
    }
}
