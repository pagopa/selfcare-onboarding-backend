package it.pagopa.selfcare.onboarding.connector;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionContact;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipInfo;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipsResponse;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.*;
import it.pagopa.selfcare.onboarding.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnBoardingInfo;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnboardingInstitutionRequest;
import it.pagopa.selfcare.onboarding.connector.rest.model.PersonInfo;
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
import static it.pagopa.selfcare.onboarding.connector.PartyConnectorImpl.REQUIRED_PRODUCT_ID_MESSAGE;
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
        BillingData billingData = mockInstance(new BillingData());
        onboardingData.setBillingData(billingData);

        // when
        partyConnector.onboardingOrganization(onboardingData);
        // then
        verify(restClientMock, times(1))
                .onboardingOrganization(onboardingRequestCaptor.capture());
        OnboardingInstitutionRequest request = onboardingRequestCaptor.getValue();
        Assertions.assertEquals(onboardingData.getInstitutionId(), request.getInstitutionExternalId());
        Assertions.assertNotNull(request.getUsers());
        Assertions.assertTrue(request.getUsers().isEmpty());
        verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void onboardingOrganization() {
        // given
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        BillingData billingData = mockInstance(new BillingData());
        onboardingData.setBillingData(billingData);
        onboardingData.setUsers(List.of(mockInstance(new User())));
        // when
        partyConnector.onboardingOrganization(onboardingData);
        // then
        verify(restClientMock, times(1))
                .onboardingOrganization(onboardingRequestCaptor.capture());
        OnboardingInstitutionRequest request = onboardingRequestCaptor.getValue();
        Assertions.assertEquals(onboardingData.getInstitutionId(), request.getInstitutionExternalId());
        Assertions.assertNotNull(request.getUsers());
        Assertions.assertEquals(1, request.getUsers().size());
        Assertions.assertEquals(onboardingData.getProductId(), request.getUsers().get(0).getProduct());
        Assertions.assertEquals(onboardingData.getUsers().get(0).getName(), request.getUsers().get(0).getName());
        Assertions.assertEquals(onboardingData.getUsers().get(0).getSurname(), request.getUsers().get(0).getSurname());
        Assertions.assertEquals(onboardingData.getUsers().get(0).getTaxCode(), request.getUsers().get(0).getTaxCode());
        Assertions.assertEquals(onboardingData.getUsers().get(0).getRole(), request.getUsers().get(0).getRole());
        Assertions.assertEquals(onboardingData.getUsers().get(0).getEmail(), request.getUsers().get(0).getEmail());
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
        Assertions.assertNotNull(institutionInfos);
        Assertions.assertTrue(institutionInfos.isEmpty());
        verify(restClientMock, times(1))
                .getOnBoardingInfo(isNull(), Mockito.isNotNull());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getOnboardedInstitutions_nullInstitutions() {
        //given
        OnBoardingInfo onboardingInfo = new OnBoardingInfo();
        PersonInfo personInfo = mockInstance(new PersonInfo());
        onboardingInfo.setPerson(personInfo);
        when(restClientMock.getOnBoardingInfo(any(), any()))
                .thenReturn(onboardingInfo);
        //when
        Collection<InstitutionInfo> institutionInfos = partyConnector.getOnBoardedInstitutions();
        //then
        Assertions.assertNotNull(institutionInfos);
        Assertions.assertTrue(institutionInfos.isEmpty());
        verify(restClientMock, times(1))
                .getOnBoardingInfo(isNull(), Mockito.isNotNull());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserInstitutionRelationships() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        RelationshipInfo relationshipInfo1 = mockInstance(new RelationshipInfo());
        RelationshipInfo relationshipInfo2 = mockInstance(new RelationshipInfo());
        RelationshipsResponse response = new RelationshipsResponse();
        response.add(relationshipInfo1);
        response.add(relationshipInfo2);
        when(restClientMock.getUserInstitutionRelationships(Mockito.anyString(),
                any(),
                any(),
                any(),
                any(),
                any()))
                .thenReturn(response);
        //when
        RelationshipsResponse restResponse = partyConnector.getUserInstitutionRelationships(institutionId, productId);
        //
        Assertions.assertNotNull(restResponse);
        Assertions.assertEquals(2, restResponse.size());
        Assertions.assertEquals(relationshipInfo1.getId(), restResponse.get(0).getId());
        Assertions.assertEquals(relationshipInfo1.getFrom(), restResponse.get(0).getFrom());
        Assertions.assertEquals(relationshipInfo1.getEmail(), restResponse.get(0).getEmail());
        Assertions.assertEquals(relationshipInfo1.getTaxCode(), restResponse.get(0).getTaxCode());
        Assertions.assertEquals(relationshipInfo1.getName(), restResponse.get(0).getName());
        verify(restClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId),
                        isNull(),
                        eq(EnumSet.of(ACTIVE)),
                        eq(Set.of(productId)),
                        isNull(),
                        isNull());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserInstitutionRelationships_emptyResponse() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";

        //when
        RelationshipsResponse response = partyConnector.getUserInstitutionRelationships(institutionId, productId);
        //then
        Assertions.assertNull(response);
        verify(restClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId),
                        isNull(),
                        eq(EnumSet.of(ACTIVE)),
                        eq(Set.of(productId)),
                        isNull(),
                        isNull());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserInstitutionRelationships_nullInstitutionId() {
        //given
        String institutionId = null;
        String productId = "productId";
        //when
        Executable executable = () -> partyConnector.getUserInstitutionRelationships(institutionId, productId);
        //then
        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals(REQUIRED_INSTITUTION_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void getUserInstitutionRelationships_nullProductId() {
        //given
        String institutionId = "institutionId";
        String productId = null;
        //when
        Executable executable = () -> partyConnector.getUserInstitutionRelationships(institutionId, productId);
        //then
        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals(REQUIRED_PRODUCT_ID_MESSAGE, e.getMessage());
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
        Assertions.assertEquals(REQUIRED_INSTITUTION_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);

    }

    @Test
    void getUsers_nullResponse_emptyRole_emptyProductIds_emptyProductRole_emptyUserId() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setAllowedState(Optional.of(EnumSet.of(ACTIVE)));
        // when
        Collection<UserInfo> users = partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        Assertions.assertNotNull(users);
        Assertions.assertTrue(users.isEmpty());
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
        Assertions.assertNotNull(users);
        Assertions.assertTrue(users.isEmpty());
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
        userInfoFilter.setAllowedState(Optional.of(EnumSet.of(ACTIVE)));
        // when
        Collection<UserInfo> users = partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        Assertions.assertNotNull(users);
        Assertions.assertTrue(users.isEmpty());
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
        userInfoFilter.setAllowedState(Optional.of(EnumSet.of(ACTIVE)));

        // when
        Collection<UserInfo> users = partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        Assertions.assertNotNull(users);
        Assertions.assertTrue(users.isEmpty());
        verify(restClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId), isNull(), Mockito.isNotNull(), isNull(), eq(userInfoFilter.getProductRoles().get()), isNull());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUsers_notEmptyRole() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setRole(Optional.of(EnumSet.of(PartyRole.MANAGER)));
        userInfoFilter.setAllowedState(Optional.of(EnumSet.of(ACTIVE)));
        // when
        Collection<UserInfo> users = partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        Assertions.assertNotNull(users);
        Assertions.assertTrue(users.isEmpty());
        verify(restClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId), Mockito.isNotNull(), Mockito.isNotNull(), isNull(), isNull(), isNull());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUsers() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setAllowedState(Optional.of(EnumSet.of(ACTIVE)));
        userInfoFilter.setRole(Optional.of(EnumSet.of(PartyRole.MANAGER)));

        RelationshipInfo relationshipInfo1 = mockInstance(new RelationshipInfo(), "setFrom");
        String id = "id";
        relationshipInfo1.setFrom(id);
        relationshipInfo1.setRole(PartyRole.MANAGER);
        //FIXME
        InstitutionContact institutionContactMock = mockInstance(new InstitutionContact());
        Map<String, List<InstitutionContact>> institutionContact = new HashMap<>();
        institutionContact.put("institutionContact", List.of(institutionContactMock));
        relationshipInfo1.setInstitutionContacts(institutionContact);
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
        Assertions.assertNotNull(userInfos);
        Assertions.assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        Assertions.assertEquals(id, userInfo.getId());
        Assertions.assertNotNull(userInfo.getName());
        Assertions.assertNotNull(userInfo.getSurname());
        Assertions.assertNotNull(userInfo.getEmail());
        Assertions.assertNotNull(userInfo.getStatus());
        Assertions.assertNotNull(userInfo.getRole());

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
        Assertions.assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        Assertions.assertEquals("user1", userInfo.getName());
        Assertions.assertEquals(PartyRole.DELEGATE, userInfo.getRole());
        Assertions.assertEquals("ACTIVE", userInfo.getStatus());
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
        Assertions.assertEquals("user1", userInfo.getName());
        Assertions.assertEquals(PartyRole.DELEGATE, userInfo.getRole());
        Assertions.assertEquals("PENDING", userInfo.getStatus());
        Assertions.assertEquals(1, userInfos.size());
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
        Assertions.assertEquals("user1", userInfo.getName());
        Assertions.assertEquals(PartyRole.OPERATOR, userInfo.getRole());
        Assertions.assertEquals("ACTIVE", userInfo.getStatus());
        Assertions.assertEquals(1, userInfos.size());
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
        Assertions.assertEquals("user1", userInfo.getName());
        Assertions.assertEquals(PartyRole.DELEGATE, userInfo.getRole());
        Assertions.assertEquals("ACTIVE", userInfo.getStatus());
        Assertions.assertEquals(1, userInfos.size());
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
        BillingData billingData = mockInstance(new BillingData());
        OnboardingResponseData onboardingData = mockInstance(new OnboardingResponseData());
        onboardingData.setId(institutionId);
        onboardingData.setBilling(billingData);
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


}