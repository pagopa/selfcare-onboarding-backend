package it.pagopa.selfcare.onboarding.connector;

import it.pagopa.selfcare.onboarding.common.InstitutionType;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.*;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.DataProtectionOfficer;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PaymentServiceProvider;
import it.pagopa.selfcare.onboarding.connector.rest.client.MsOnboardingApiClient;
import it.pagopa.selfcare.onboarding.connector.rest.client.MsOnboardingSupportApiClient;
import it.pagopa.selfcare.onboarding.connector.rest.client.MsOnboardingTokenApiClient;
import it.pagopa.selfcare.onboarding.connector.rest.mapper.OnboardingMapper;
import it.pagopa.selfcare.onboarding.connector.rest.mapper.OnboardingMapperImpl;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
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
class OnboardingMsConnectorImplTest {

    @InjectMocks
    private OnboardingMsConnectorImpl onboardingMsConnector;

    @Mock
    private MsOnboardingApiClient msOnboardingApiClient;

    @Mock
    private MsOnboardingTokenApiClient msOnboardingTokenApiClient;

    @Mock
    private MsOnboardingSupportApiClient msOnboardingSupportApiClient;

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
        onboardingData.setOriginId("originId");
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
        onboardingData.setProductId("produictId");
        onboardingData.setTaxCode("taxCode");
        onboardingData.setInstitutionType(InstitutionType.PG);
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setTaxCode("taxCode");
        institutionUpdate.setDescription("description");
        onboardingData.setUsers(List.of(mockInstance(new User())));
        onboardingData.setInstitutionUpdate(institutionUpdate);
        // when
        onboardingMsConnector.onboardingCompany(onboardingData);
        // then
        ArgumentCaptor<OnboardingPgRequest> onboardingRequestCaptor = ArgumentCaptor.forClass(OnboardingPgRequest.class);
        verify(msOnboardingApiClient, times(1))
                ._v1OnboardingPgCompletionPost(onboardingRequestCaptor.capture());
        OnboardingPgRequest actual = onboardingRequestCaptor.getValue();
        assertEquals(actual.getProductId(), onboardingData.getProductId());
        assertEquals(actual.getTaxCode(), institutionUpdate.getTaxCode());
        assertEquals(actual.getDigitalAddress(), institutionUpdate.getDigitalAddress());
        assertEquals(actual.getBusinessName(), institutionUpdate.getDescription());
        assertEquals(actual.getInstitutionType().getValue(), onboardingData.getInstitutionType().name());
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
    void onboardingUsersComplete() throws IOException {
        // given
        final String tokenId = "tokenId";
        final MockMultipartFile mockMultipartFile =
                new MockMultipartFile("example", new ByteArrayInputStream("example".getBytes(StandardCharsets.UTF_8)));
        // when
        final Executable executable = () -> msOnboardingApiClient._v1OnboardingOnboardingIdCompleteOnboardingUsersPut(tokenId, mockMultipartFile);
        // then
        assertDoesNotThrow(executable);
        verify(msOnboardingApiClient, times(1))
                ._v1OnboardingOnboardingIdCompleteOnboardingUsersPut(tokenId, mockMultipartFile);
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
        final String reason = "reason";
        final String onboardingId = "onboardingId";
        ReasonRequest reasonDto = new ReasonRequest();
        reasonDto.setReasonForReject(reason);
        // when
        final Executable executable = () -> onboardingMsConnector.rejectOnboarding(onboardingId, reason);
        // then
        assertDoesNotThrow(executable);
        verify(msOnboardingApiClient, times(1))
                ._v1OnboardingOnboardingIdRejectPut(onboardingId, reasonDto);
        verifyNoMoreInteractions(msOnboardingApiClient);
    }

    @Test
    void rejectOnboardingWithNoReason() {
        // given
        final String onboardingId = "onboardingId";
        ReasonRequest reasonDto = new ReasonRequest();
        // when
        final Executable executable = () -> onboardingMsConnector.rejectOnboarding(onboardingId, "");
        // then
        assertDoesNotThrow(executable);
        verify(msOnboardingApiClient, times(1))
                ._v1OnboardingOnboardingIdRejectPut(onboardingId, reasonDto);
        verifyNoMoreInteractions(msOnboardingApiClient);
    }

    @Test
    void getContract() {
        // given
        final String onboardingId = "onboardingId";
        Resource resource = Mockito.mock(Resource.class);
        when(msOnboardingTokenApiClient._v1TokensOnboardingIdContractGet(onboardingId))
                .thenReturn(ResponseEntity.of(Optional.of(resource)));
        // when
        final Executable executable = () -> onboardingMsConnector.getContract(onboardingId);
        // then
        assertDoesNotThrow(executable);
        verify(msOnboardingTokenApiClient, times(1))
                ._v1TokensOnboardingIdContractGet(onboardingId);
        verifyNoMoreInteractions(msOnboardingTokenApiClient);
    }

    @Test
    void onboardingUsers() {
        // given
        final String origin = "origin";
        final String originId = "originId";
        OnboardingData onboardingData = new OnboardingData();
        onboardingData.setOrigin(origin);
        onboardingData.setOriginId(originId);
        OnboardingUserRequest request = new OnboardingUserRequest();
        request.setOrigin(origin);
        request.setOriginId(originId);
        OnboardingResponse resource = Mockito.mock(OnboardingResponse.class);
        when(msOnboardingApiClient._v1OnboardingUsersPost(request))
                .thenReturn(ResponseEntity.of(Optional.of(resource)));
        // when
        final Executable executable = () -> onboardingMsConnector.onboardingUsers(onboardingData);
        // then
        assertDoesNotThrow(executable);
        verify(msOnboardingApiClient, times(1))
                ._v1OnboardingUsersPost(request);
        verifyNoMoreInteractions(msOnboardingApiClient);
    }

    @Test
    void onboardingPaAggregation() {
        // given
        OnboardingData onboardingData = new OnboardingData();
        onboardingData.setProductId("produictId");
        onboardingData.setTaxCode("taxCode");
        onboardingData.setInstitutionType(InstitutionType.PG);
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setTaxCode("taxCode");
        institutionUpdate.setDescription("description");
        onboardingData.setUsers(List.of(mockInstance(new User())));
        onboardingData.setInstitutionUpdate(institutionUpdate);
        Institution institution = new Institution();
        institution.setTaxCode("taxCode");
        institution.setDescription("description");
        onboardingData.setAggregates(List.of(institution));
        onboardingData.setIsAggregator(Boolean.TRUE);
        // when
        onboardingMsConnector.onboardingPaAggregation(onboardingData);
        // then
        ArgumentCaptor<OnboardingPaRequest> onboardingRequestCaptor = ArgumentCaptor.forClass(OnboardingPaRequest.class);
        verify(msOnboardingApiClient, times(1))
                ._v1OnboardingPaAggregationPost(onboardingRequestCaptor.capture());
        OnboardingPaRequest actual = onboardingRequestCaptor.getValue();
        assertEquals(actual.getProductId(), onboardingData.getProductId());
        assertEquals(actual.getAggregates().size(), onboardingData.getAggregates().size());
        assertEquals(actual.getAggregates().get(0).getTaxCode(), onboardingData.getAggregates().get(0).getTaxCode());
        assertEquals(actual.getAggregates().get(0).getDescription(), onboardingData.getAggregates().get(0).getDescription());
        assertEquals(actual.getInstitution().getTaxCode(), onboardingData.getTaxCode());
        assertEquals(actual.getInstitution().getTaxCode(), onboardingData.getInstitutionUpdate().getTaxCode() );
        assertEquals(actual.getInstitution().getDescription(), onboardingData.getInstitutionUpdate().getDescription());
        assertEquals(actual.getUsers().size(), onboardingData.getUsers().size());

        verifyNoMoreInteractions(msOnboardingApiClient);
    }

    @Test
    void getOnboardingByFilters() {
        // given
        final String origin = "origin";
        final String originId = "originId";
        final String productId = "productId";
        OnboardingResponse resource = new OnboardingResponse();
        resource.setInstitution(new InstitutionResponse());
        resource.setProductId("productId");
        when(msOnboardingSupportApiClient._v1OnboardingInstitutionOnboardingsGet(origin, originId, OnboardingStatus.COMPLETED, null, null))
                .thenReturn(ResponseEntity.ok(List.of(resource)));
        // when
        final Executable executable = () -> onboardingMsConnector.getByFilters(productId, null, origin, originId, null);
        // then
        assertDoesNotThrow(executable);
        verify(msOnboardingSupportApiClient, times(1))
                ._v1OnboardingInstitutionOnboardingsGet(origin, originId, OnboardingStatus.COMPLETED, null, null);
        verifyNoMoreInteractions(msOnboardingSupportApiClient);
    }

    @Test
    void getOnboardingByFiltersWithUO() {
        // given
        final String origin = "origin";
        final String originId = "originId";
        final String productId = "productId";
        OnboardingResponse resource = new OnboardingResponse();
        InstitutionResponse institution = new InstitutionResponse();
        institution.setSubunitType(InstitutionPaSubunitType.UO);
        resource.setInstitution(institution);

        resource.setProductId("productId");
        when(msOnboardingSupportApiClient._v1OnboardingInstitutionOnboardingsGet(origin, originId, OnboardingStatus.COMPLETED, null, null))
                .thenReturn(ResponseEntity.ok(List.of(resource)));
        // when
        List<OnboardingData> result = onboardingMsConnector.getByFilters(productId, null, origin, originId, null);
        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(msOnboardingSupportApiClient, times(1))
                ._v1OnboardingInstitutionOnboardingsGet(origin, originId, OnboardingStatus.COMPLETED, null, null);
        verifyNoMoreInteractions(msOnboardingSupportApiClient);
    }

    @Test
    void getOnboardingByFiltersWithAOO() {
        // given
        final String origin = "origin";
        final String originId = "originId";
        final String productId = "productId";
        OnboardingResponse resource = new OnboardingResponse();
        InstitutionResponse institution = new InstitutionResponse();
        institution.setSubunitType(InstitutionPaSubunitType.AOO);
        resource.setInstitution(institution);

        resource.setProductId("productId");
        when(msOnboardingSupportApiClient._v1OnboardingInstitutionOnboardingsGet(origin, originId, OnboardingStatus.COMPLETED, null, null))
                .thenReturn(ResponseEntity.ok(List.of(resource)));
        // when
        List<OnboardingData> result = onboardingMsConnector.getByFilters(productId, null, origin, originId, null);
        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(msOnboardingSupportApiClient, times(1))
                ._v1OnboardingInstitutionOnboardingsGet(origin, originId, OnboardingStatus.COMPLETED, null, null);
        verifyNoMoreInteractions(msOnboardingSupportApiClient);
    }

    @Test
    void checkManager() {
        // given
        final String origin = "origin";
        final String originId = "originId";
        OnboardingData onboardingData = new OnboardingData();
        onboardingData.setOrigin(origin);
        onboardingData.setOriginId(originId);
        OnboardingUserRequest request = new OnboardingUserRequest();
        request.setOrigin(origin);
        request.setOriginId(originId);
        when(msOnboardingApiClient._v1OnboardingCheckManagerPost(request))
                .thenReturn(ResponseEntity.of(Optional.of("true")));
        // when
        final Executable executable = () -> onboardingMsConnector.checkManager(onboardingData);
        // then
        assertDoesNotThrow(executable);
        verify(msOnboardingApiClient, times(1))
                ._v1OnboardingCheckManagerPost(request);
        verifyNoMoreInteractions(msOnboardingApiClient);
    }
}
