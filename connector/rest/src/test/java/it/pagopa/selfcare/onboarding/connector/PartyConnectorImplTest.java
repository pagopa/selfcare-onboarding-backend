package it.pagopa.selfcare.onboarding.connector;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipInfo;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipState;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipsResponse;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.*;
import it.pagopa.selfcare.onboarding.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnBoardingInfo;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnboardingInstitutionRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.onboarding.connector.PartyConnectorImpl.REQUIRED_INSTITUTION_ID_MESSAGE;
import static it.pagopa.selfcare.onboarding.connector.model.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.onboarding.connector.model.RelationshipState.PENDING;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartyConnectorImplTest {

    @InjectMocks
    private PartyConnectorImpl partyConnector;

    @Mock
    private PartyProcessRestClient restClientMock;

    @Captor
    ArgumentCaptor<OnboardingInstitutionRequest> onboardingRequestCaptor;

    private final ObjectMapper mapper;

    public PartyConnectorImplTest() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setTimeZone(TimeZone.getDefault());
    }

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
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Billing billing = mockInstance(new Billing());
        onboardingData.setBilling(billing);

        // when
        partyConnector.onboardingOrganization(onboardingData);
        // then
        verify(restClientMock, times(1))
                .onboardingOrganization(onboardingRequestCaptor.capture());
        OnboardingInstitutionRequest request = onboardingRequestCaptor.getValue();
        assertEquals(onboardingData.getInstitutionExternalId(), request.getInstitutionExternalId());
        assertNotNull(request.getUsers());
        assertTrue(request.getUsers().isEmpty());
        verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void onboardingOrganization() {
        // given
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Billing billing = mockInstance(new Billing());
        InstitutionUpdate institutionUpdate = mockInstance(new InstitutionUpdate());
        onboardingData.setInstitutionUpdate(institutionUpdate);
        onboardingData.setBilling(billing);
        onboardingData.setUsers(List.of(mockInstance(new User())));
        // when
        partyConnector.onboardingOrganization(onboardingData);
        // then
        verify(restClientMock, times(1))
                .onboardingOrganization(onboardingRequestCaptor.capture());
        OnboardingInstitutionRequest request = onboardingRequestCaptor.getValue();
        assertEquals(onboardingData.getInstitutionExternalId(), request.getInstitutionExternalId());
        assertNotNull(request.getUsers());
        assertEquals(1, request.getUsers().size());
        TestUtils.reflectionEqualsByName(institutionUpdate, request.getInstitutionUpdate());
        TestUtils.reflectionEqualsByName(billing, request.getBilling());
        assertEquals(onboardingData.getProductId(), request.getUsers().get(0).getProduct());
        assertEquals(onboardingData.getUsers().get(0).getName(), request.getUsers().get(0).getName());
        assertEquals(onboardingData.getUsers().get(0).getSurname(), request.getUsers().get(0).getSurname());
        assertEquals(onboardingData.getUsers().get(0).getTaxCode(), request.getUsers().get(0).getTaxCode());
        assertEquals(onboardingData.getUsers().get(0).getRole(), request.getUsers().get(0).getRole());
        assertEquals(onboardingData.getUsers().get(0).getEmail(), request.getUsers().get(0).getEmail());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getOnboardedInstitutions() {
        // given
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingResponseData onboardingData1 = mockInstance(new OnboardingResponseData(), 1, "setState");
        onboardingData1.setState(ACTIVE);
        OnboardingResponseData onboardingData2 = mockInstance(new OnboardingResponseData(), 2, "setState", "setId");
        onboardingData2.setState(PENDING);
        onboardingData2.setId(onboardingData1.getId());
        OnboardingResponseData onboardingData3 = mockInstance(new OnboardingResponseData(), 3, "setState");
        onboardingData3.setState(PENDING);
        onBoardingInfo.setInstitutions(List.of(onboardingData1, onboardingData2, onboardingData3, onboardingData3));
        when(restClientMock.getOnBoardingInfo(any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        Collection<InstitutionInfo> institutions = partyConnector.getOnBoardedInstitutions();
        // then
        assertNotNull(institutions);
        assertEquals(2, institutions.size());
        Map<String, List<InstitutionInfo>> map = institutions.stream()
                .collect(Collectors.groupingBy(InstitutionInfo::getStatus));
        List<InstitutionInfo> institutionInfos = map.get(ACTIVE.name());
        assertNotNull(institutionInfos);
        assertEquals(1, institutionInfos.size());
        assertEquals(onboardingData1.getId(), institutionInfos.get(0).getId());
        assertEquals(onboardingData1.getDescription(), institutionInfos.get(0).getDescription());
        assertEquals(onboardingData1.getExternalId(), institutionInfos.get(0).getExternalId());
        assertEquals(onboardingData1.getState().toString(), institutionInfos.get(0).getStatus());
        institutionInfos = map.get(PENDING.name());
        assertNotNull(institutionInfos);
        assertEquals(1, institutionInfos.size());
        assertEquals(onboardingData3.getId(), institutionInfos.get(0).getId());
        assertEquals(onboardingData3.getDescription(), institutionInfos.get(0).getDescription());
        assertEquals(onboardingData3.getExternalId(), institutionInfos.get(0).getExternalId());
        assertEquals(onboardingData3.getState().toString(), institutionInfos.get(0).getStatus());
        verify(restClientMock, times(1))
                .getOnBoardingInfo(isNull(), eq(EnumSet.of(ACTIVE)));
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getOnboardedInstitutions_nullOnboardingInfo() {
        //given
        //when
        Collection<InstitutionInfo> institutionInfos = partyConnector.getOnBoardedInstitutions();
        //then
        assertNotNull(institutionInfos);
        assertTrue(institutionInfos.isEmpty());
        verify(restClientMock, times(1))
                .getOnBoardingInfo(isNull(), Mockito.isNotNull());
        verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getOnboardedInstitutions_nullInstitutions() {
        //given
        OnBoardingInfo onboardingInfo = new OnBoardingInfo();
        when(restClientMock.getOnBoardingInfo(any(), any()))
                .thenReturn(onboardingInfo);
        //when
        Collection<InstitutionInfo> institutionInfos = partyConnector.getOnBoardedInstitutions();
        //then
        assertNotNull(institutionInfos);
        assertTrue(institutionInfos.isEmpty());
        verify(restClientMock, times(1))
                .getOnBoardingInfo(isNull(), Mockito.isNotNull());
        verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getUserInstitutionRelationships() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        final EnumSet<RelationshipState> allowedStates = EnumSet.of(ACTIVE);
        final EnumSet<PartyRole> roles = EnumSet.of(PartyRole.MANAGER);
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(Optional.of(productId));
        userInfoFilter.setAllowedStates(Optional.of(allowedStates));
        userInfoFilter.setRole(Optional.of(roles));
        RelationshipInfo relationshipInfo1 = mockInstance(new RelationshipInfo());
        RelationshipInfo relationshipInfo2 = mockInstance(new RelationshipInfo());
        RelationshipsResponse response = new RelationshipsResponse();
        response.add(relationshipInfo1);
        response.add(relationshipInfo2);
        when(restClientMock.getUserInstitutionRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(response);
        //when
        RelationshipsResponse restResponse = partyConnector.getUserInstitutionRelationships(institutionId, userInfoFilter);
        //
        assertNotNull(restResponse);
        assertEquals(2, restResponse.size());
        assertIterableEquals(response, restResponse);
        verify(restClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId), eq(roles), eq(allowedStates), eq(Set.of(productId)), isNull(), isNull());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserInstitutionRelationships_emptyResponse() {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        //when
        RelationshipsResponse response = partyConnector.getUserInstitutionRelationships(institutionId, userInfoFilter);
        //then
        Assertions.assertNull(response);
        verify(restClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId), isNull(), isNull(), isNull(), isNull(), isNull());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserInstitutionRelationships_nullInstitutionId() {
        //given
        String institutionId = null;
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        //when
        Executable executable = () -> partyConnector.getUserInstitutionRelationships(institutionId, userInfoFilter);
        //then
        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void getUserInstitutionRelationships_nullFilter() {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = null;
        //when
        Executable executable = () -> partyConnector.getUserInstitutionRelationships(institutionId, userInfoFilter);
        //then
        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A filter is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void getUsers_nullInstitutionId() {
        //given
        String institutionId = null;
        UserInfo.UserInfoFilter filter = new UserInfo.UserInfoFilter();
        //when
        Executable executable = () -> partyConnector.getUsers(institutionId, filter);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);

    }

    @Test
    void getUsers_nullResponse_emptyRole_emptyProductIds_emptyProductRole_emptyUserId() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setAllowedStates(Optional.of(EnumSet.of(ACTIVE)));
        // when
        Collection<UserInfo> users = partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(restClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId), isNull(), Mockito.notNull(), isNull(), isNull(), isNull());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUsers_nullResponse() {
        // given
        PartyConnectorImpl partyConnector = new PartyConnectorImpl(restClientMock);

        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        // when
        Collection<UserInfo> users = partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(restClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId), isNull(), isNull(), isNull(), isNull(), isNull());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUsers_notEmptyProductIds() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(Optional.of("productId"));
        userInfoFilter.setAllowedStates(Optional.of(EnumSet.of(ACTIVE)));
        // when
        Collection<UserInfo> users = partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(restClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId), isNull(), Mockito.notNull(), eq(userInfoFilter.getProductId().map(Set::of).get()), isNull(), isNull());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUsers_notEmptyProductRoles() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductRoles(Optional.of(Set.of("api", "security")));
        userInfoFilter.setAllowedStates(Optional.of(EnumSet.of(ACTIVE)));

        // when
        Collection<UserInfo> users = partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(restClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId), isNull(), isNotNull(), isNull(), eq(userInfoFilter.getProductRoles().get()), isNull());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUsers_notEmptyRole() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setRole(Optional.of(EnumSet.of(PartyRole.MANAGER)));
        userInfoFilter.setAllowedStates(Optional.of(EnumSet.of(ACTIVE)));
        // when
        Collection<UserInfo> users = partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(restClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId), Mockito.isNotNull(), Mockito.isNotNull(), isNull(), isNull(), isNull());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUsers() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setAllowedStates(Optional.of(EnumSet.of(ACTIVE)));
        userInfoFilter.setRole(Optional.of(EnumSet.of(PartyRole.MANAGER)));

        RelationshipInfo relationshipInfo1 = mockInstance(new RelationshipInfo(), "setFrom");
        String id = "id";
        relationshipInfo1.setFrom(id);
        relationshipInfo1.setRole(PartyRole.MANAGER);
        RelationshipInfo relationshipInfo2 = mockInstance(new RelationshipInfo(), "setFrom");
        relationshipInfo2.setFrom(id);
        relationshipInfo2.setRole(PartyRole.DELEGATE);
        RelationshipsResponse relationshipsResponse = new RelationshipsResponse();
        relationshipsResponse.add(relationshipInfo1);
        relationshipsResponse.add(relationshipInfo2);
        when(restClientMock.getUserInstitutionRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationshipsResponse);
        // when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        assertNotNull(userInfos);
        assertEquals(1, userInfos.size());
        userInfos.forEach(userInfo -> {
            assertEquals(id, userInfo.getId());
            assertNull(userInfo.getUser());
            assertNotNull(userInfo.getStatus());
            assertNotNull(userInfo.getRole());
            assertNotNull(userInfo.getInstitutionId());
        });

        verify(restClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId), Mockito.isNotNull(), Mockito.notNull(), isNull(), isNull(), any());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUsers_higherRoleForActiveUsers() throws IOException {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUserInstitutionRelationships/higher-role-active.json");
        RelationshipsResponse relationshipsResponse = mapper.readValue(stub, RelationshipsResponse.class);

        when(restClientMock.getUserInstitutionRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
        //Then
        assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        assertEquals(PartyRole.DELEGATE, userInfo.getRole());
        assertEquals("ACTIVE", userInfo.getStatus());
    }

    @Test
    void getUsers_higherRoleForPendingUsers() throws IOException {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();


        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUserInstitutionRelationships/higher-role-pending.json");
        RelationshipsResponse relationshipsResponse = mapper.readValue(stub, RelationshipsResponse.class);

        when(restClientMock.getUserInstitutionRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
        UserInfo userInfo = userInfos.iterator().next();
        //Then
        assertEquals(PartyRole.DELEGATE, userInfo.getRole());
        assertEquals("PENDING", userInfo.getStatus());
        assertEquals(1, userInfos.size());
    }

    @Test
    void getUsers_activeRoleUserDifferentStatus() throws IOException {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUserInstitutionRelationships/active-role-different-status.json");
        RelationshipsResponse relationshipsResponse = mapper.readValue(stub, RelationshipsResponse.class);
        when(restClientMock.getUserInstitutionRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
        UserInfo userInfo = userInfos.iterator().next();
        //Then
        assertEquals(PartyRole.OPERATOR, userInfo.getRole());
        assertEquals("ACTIVE", userInfo.getStatus());
        assertEquals(1, userInfos.size());
    }

    @Test
    void getUsers_activeRoleUserDifferentStatus2() throws IOException {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUserInstitutionRelationships/active-role-different-status-2.json");
        RelationshipsResponse relationshipsResponse = mapper.readValue(stub, RelationshipsResponse.class);

        when(restClientMock.getUserInstitutionRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
        UserInfo userInfo = userInfos.iterator().next();
        //Then
        assertEquals(PartyRole.DELEGATE, userInfo.getRole());
        assertEquals("ACTIVE", userInfo.getStatus());
        assertEquals(1, userInfos.size());
    }

    @Test
    void getInstitution() {
        //given
        String institutionId = "institutionId";
        Institution institutionMock = mockInstance(new Institution());
        when(restClientMock.getInstitutionByExternalId(Mockito.anyString()))
                .thenReturn(institutionMock);
        //when
        Institution institution = partyConnector.getInstitutionByExternalId(institutionId);
        //then
        assertNotNull(institution);
        assertEquals(institutionMock.getExternalId(), institution.getExternalId());
        assertEquals(institutionMock.getDescription(), institution.getDescription());
        assertEquals(institutionMock.getAddress(), institution.getAddress());
        assertEquals(institutionMock.getTaxCode(), institution.getTaxCode());
        assertEquals(institutionMock.getId(), institution.getId());
        assertEquals(institutionMock.getZipCode(), institution.getZipCode());
        assertEquals(institutionMock.getDigitalAddress(), institution.getDigitalAddress());
        assertEquals(institutionMock.getInstitutionType(), institution.getInstitutionType());
        verify(restClientMock, times(1))
                .getInstitutionByExternalId(institutionId);
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getInstitution_nullInstitutionId() {
        //given
        String institutionId = null;
        //when
        Executable exe = () -> partyConnector.getInstitutionByExternalId(institutionId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, exe);
        assertEquals(REQUIRED_INSTITUTION_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void getOnboardedInstitution() {
        //given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = mockInstance(new OnBoardingInfo());
        Billing billing = mockInstance(new Billing());
        OnboardingResponseData onboardingData = mockInstance(new OnboardingResponseData());
        onboardingData.setId(institutionId);
        onboardingData.setBilling(billing);
        onBoardingInfo.setInstitutions(Collections.singletonList(onboardingData));
        when(restClientMock.getOnBoardingInfo(any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = partyConnector.getOnboardedInstitution(institutionId);
        // then
        assertNotNull(institutionInfo);
        assertEquals(onboardingData.getId(), institutionInfo.getId());
        assertEquals(onboardingData.getDescription(), institutionInfo.getDescription());
        assertEquals(onboardingData.getDigitalAddress(), institutionInfo.getDigitalAddress());
        assertEquals(onboardingData.getExternalId(), institutionInfo.getExternalId());
        assertEquals(onboardingData.getState().toString(), institutionInfo.getStatus());
        assertEquals(onboardingData.getAddress(), institutionInfo.getAddress());
        assertEquals(onboardingData.getBilling().getRecipientCode(), institutionInfo.getBilling().getRecipientCode());
        assertEquals(onboardingData.getBilling().getPublicServices(), institutionInfo.getBilling().getPublicServices());
        assertEquals(onboardingData.getBilling().getVatNumber(), institutionInfo.getBilling().getVatNumber());

        verify(restClientMock, times(1))
                .getOnBoardingInfo(institutionId, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getOnboardedInstitution_emptyInstitutions() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        onBoardingInfo.setInstitutions(Collections.emptyList());
        when(restClientMock.getOnBoardingInfo(any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = partyConnector.getOnboardedInstitution(institutionId);
        // then
        assertNull(institutionInfo);
        verify(restClientMock, times(1))
                .getOnBoardingInfo(institutionId, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getOnboardedInstitution_nullOnBoardingInfo() {
        // given
        String institutionId = "institutionId";
        // when
        InstitutionInfo institutionInfo = partyConnector.getOnboardedInstitution(institutionId);
        // then
        assertNull(institutionInfo);
        verify(restClientMock, times(1))
                .getOnBoardingInfo(institutionId, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getOnboardedInstitution_nullInstitutionId() {
        //given
        String institutionId = null;
        //when
        Executable executable = () -> partyConnector.getOnboardedInstitution(institutionId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void getOnboardedInstitution_nullInstitutions() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        when(restClientMock.getOnBoardingInfo(any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = partyConnector.getOnboardedInstitution(institutionId);
        // then
        assertNull(institutionInfo);
        verify(restClientMock, times(1))
                .getOnBoardingInfo(institutionId, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void createInstitutionUsingExternalId_nullId() {
        //given
        String externalId = null;
        //when
        Executable executable = () -> partyConnector.createInstitutionUsingExternalId(externalId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_ID_MESSAGE, e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @Test
    void createInstitutionUsingExternalId() {
        //given
        String externalId = "externalId";
        Institution institution = TestUtils.mockInstance(new Institution());
        when(restClientMock.createInstitutionUsingExternalId(Mockito.anyString()))
                .thenReturn(institution);
        //when
        Institution result = partyConnector.createInstitutionUsingExternalId(externalId);
        //then
        assertNotNull(result);
        assertSame(institution, result);
        verify(restClientMock, times(1))
                .createInstitutionUsingExternalId(externalId);
    }

}