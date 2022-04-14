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
import it.pagopa.selfcare.onboarding.connector.model.RelationshipsResponse;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.*;
import it.pagopa.selfcare.onboarding.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnBoardingInfo;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnboardingRequest;
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

import static it.pagopa.selfcare.onboarding.connector.PartyConnectorImpl.REQUIRED_INSTITUTION_ID_MESSAGE;
import static it.pagopa.selfcare.onboarding.connector.PartyConnectorImpl.REQUIRED_PRODUCT_ID_MESSAGE;
import static it.pagopa.selfcare.onboarding.connector.model.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.onboarding.connector.model.RelationshipState.PENDING;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PartyConnectorImplTest {

    @InjectMocks
    private PartyConnectorImpl partyConnector;

    @Mock
    private PartyProcessRestClient restClientMock;

    @Captor
    ArgumentCaptor<OnboardingRequest> onboardingRequestCaptor;

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
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        BillingData billingData = TestUtils.mockInstance(new BillingData());
        onboardingData.setBillingData(billingData);

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
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        BillingData billingData = TestUtils.mockInstance(new BillingData());
        onboardingData.setBillingData(billingData);
        onboardingData.setUsers(List.of(TestUtils.mockInstance(new User())));
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
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getOnboardedInstitutions() {
        // given
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingResponseData onboardingData1 = TestUtils.mockInstance(new OnboardingResponseData(), 1, "setState");
        onboardingData1.setState(ACTIVE);
        OnboardingResponseData onboardingData2 = TestUtils.mockInstance(new OnboardingResponseData(), 2, "setState", "setInstitutionId");
        onboardingData2.setState(PENDING);
        onboardingData2.setInstitutionId(onboardingData1.getInstitutionId());
        OnboardingResponseData onboardingData3 = TestUtils.mockInstance(new OnboardingResponseData(), 3, "setState");
        onboardingData3.setState(PENDING);
        onBoardingInfo.setInstitutions(List.of(onboardingData1, onboardingData2, onboardingData3, onboardingData3));
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any(), Mockito.any()))
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
        assertEquals(onboardingData1.getDescription(), institutionInfos.get(0).getDescription());
        assertEquals(onboardingData1.getInstitutionId(), institutionInfos.get(0).getInstitutionId());
        assertEquals(onboardingData1.getState().toString(), institutionInfos.get(0).getStatus());
        institutionInfos = map.get(PENDING.name());
        assertNotNull(institutionInfos);
        assertEquals(1, institutionInfos.size());
        assertEquals(onboardingData3.getDescription(), institutionInfos.get(0).getDescription());
        assertEquals(onboardingData3.getInstitutionId(), institutionInfos.get(0).getInstitutionId());
        assertEquals(onboardingData3.getState().toString(), institutionInfos.get(0).getStatus());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(Mockito.isNull(), Mockito.eq(EnumSet.of(ACTIVE)));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getOnboardedInstitutions_nullOnboardingInfo() {
        //given
        //when
        Collection<InstitutionInfo> institutionInfos = partyConnector.getOnBoardedInstitutions();
        //then
        Assertions.assertNotNull(institutionInfos);
        Assertions.assertTrue(institutionInfos.isEmpty());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(Mockito.isNull(), Mockito.isNotNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getOnboardedInstitutions_nullInstitutions() {
        //given
        OnBoardingInfo onboardingInfo = new OnBoardingInfo();
        PersonInfo personInfo = TestUtils.mockInstance(new PersonInfo());
        onboardingInfo.setPerson(personInfo);
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any(), Mockito.any()))
                .thenReturn(onboardingInfo);
        //when
        Collection<InstitutionInfo> institutionInfos = partyConnector.getOnBoardedInstitutions();
        //then
        Assertions.assertNotNull(institutionInfos);
        Assertions.assertTrue(institutionInfos.isEmpty());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(Mockito.isNull(), Mockito.isNotNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUserInstitutionRelationships() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        RelationshipInfo relationshipInfo1 = TestUtils.mockInstance(new RelationshipInfo());
        RelationshipInfo relationshipInfo2 = TestUtils.mockInstance(new RelationshipInfo());
        RelationshipsResponse response = new RelationshipsResponse();
        response.add(relationshipInfo1);
        response.add(relationshipInfo2);
        Mockito.when(restClientMock.getUserInstitutionRelationships(Mockito.anyString(),
                        Mockito.any(),
                        Mockito.any(),
                        Mockito.any(),
                        Mockito.any(),
                        Mockito.any()))
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
        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserInstitutionRelationships(Mockito.eq(institutionId),
                        Mockito.isNull(),
                        Mockito.eq(EnumSet.of(ACTIVE)),
                        Mockito.eq(Set.of(productId)),
                        Mockito.isNull(),
                        Mockito.isNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
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
        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserInstitutionRelationships(Mockito.eq(institutionId),
                        Mockito.isNull(),
                        Mockito.eq(EnumSet.of(ACTIVE)),
                        Mockito.eq(Set.of(productId)),
                        Mockito.isNull(),
                        Mockito.isNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
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
        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserInstitutionRelationships(Mockito.eq(institutionId), Mockito.isNull(), Mockito.notNull(), Mockito.isNull(), Mockito.isNull(), Mockito.isNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
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
        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserInstitutionRelationships(Mockito.eq(institutionId), Mockito.isNull(), Mockito.isNull(), Mockito.isNull(), Mockito.isNull(), Mockito.isNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
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
        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserInstitutionRelationships(Mockito.eq(institutionId), Mockito.isNull(), Mockito.notNull(), Mockito.eq(userInfoFilter.getProductId().map(Set::of).get()), Mockito.isNull(), Mockito.isNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
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
        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserInstitutionRelationships(Mockito.eq(institutionId), Mockito.isNull(), Mockito.isNotNull(), Mockito.isNull(), Mockito.eq(userInfoFilter.getProductRoles().get()), Mockito.isNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
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
        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserInstitutionRelationships(Mockito.eq(institutionId), Mockito.isNotNull(), Mockito.isNotNull(), Mockito.isNull(), Mockito.isNull(), Mockito.isNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUsers() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setAllowedState(Optional.of(EnumSet.of(ACTIVE)));
        userInfoFilter.setRole(Optional.of(EnumSet.of(PartyRole.MANAGER)));

        RelationshipInfo relationshipInfo1 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        String id = "id";
        relationshipInfo1.setFrom(id);
        relationshipInfo1.setRole(PartyRole.MANAGER);
        RelationshipInfo relationshipInfo2 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        relationshipInfo2.setFrom(id);
        relationshipInfo2.setRole(PartyRole.DELEGATE);
        RelationshipsResponse relationshipsResponse = new RelationshipsResponse();
        relationshipsResponse.add(relationshipInfo1);
        relationshipsResponse.add(relationshipInfo2);
        Mockito.when(restClientMock.getUserInstitutionRelationships(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
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

        Mockito.verify(restClientMock, Mockito.times(1))
                .getUserInstitutionRelationships(Mockito.eq(institutionId), Mockito.isNotNull(), Mockito.notNull(), Mockito.isNull(), Mockito.isNull(), Mockito.any());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUsers_activeRoleUserDifferentStatus2() {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        RelationshipInfo relationshipInfo1 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        String id = "id";
        relationshipInfo1.setFrom(id);
        relationshipInfo1.setName("user1");
        relationshipInfo1.setRole(PartyRole.OPERATOR);
        relationshipInfo1.setState(PENDING);
        RelationshipInfo relationshipInfo2 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        relationshipInfo2.setFrom(id);
        relationshipInfo2.setName("user1");
        relationshipInfo2.setRole(PartyRole.DELEGATE);
        relationshipInfo2.setState(ACTIVE);
        RelationshipsResponse relationshipsResponse = new RelationshipsResponse();
        relationshipsResponse.add(relationshipInfo1);
        relationshipsResponse.add(relationshipInfo2);
        Mockito.when(restClientMock.getUserInstitutionRelationships(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
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
    void getUsers_higherRoleForPendingUsers() throws IOException {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();


        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUserInstitutionRelationships/higher-role-pending.json");
        RelationshipsResponse relationshipsResponse = mapper.readValue(stub, RelationshipsResponse.class);

        Mockito.when(restClientMock.getUserInstitutionRelationships(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
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
    void getUsers_higherRoleForActiveUsers() throws IOException {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUserInstitutionRelationships/higher-role-active.json");
        RelationshipsResponse relationshipsResponse = mapper.readValue(stub, RelationshipsResponse.class);

        Mockito.when(restClientMock.getUserInstitutionRelationships(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
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
    void getInstitution() {
        //given
        String institutionId = "institutionId";
        Institution institutionMock = TestUtils.mockInstance(new Institution());
        Mockito.when(restClientMock.getInstitutionByExternalId(Mockito.anyString()))
                .thenReturn(institutionMock);
        //when
        Institution institution = partyConnector.getInstitutionByExternalId(institutionId);
        //then
        assertNotNull(institution);
        assertEquals(institutionMock.getInstitutionId(), institution.getInstitutionId());
        assertEquals(institutionMock.getDescription(), institution.getDescription());
        assertEquals(institutionMock.getAddress(), institution.getAddress());
        assertEquals(institutionMock.getTaxCode(), institution.getTaxCode());
        assertEquals(institutionMock.getId(), institution.getId());
        assertEquals(institutionMock.getZipCode(), institution.getZipCode());
        assertEquals(institutionMock.getDigitalAddress(), institution.getDigitalAddress());
        assertEquals(institutionMock.getType(), institution.getType());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getInstitutionByExternalId(institutionId);
        Mockito.verifyNoMoreInteractions(restClientMock);
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
        OnBoardingInfo onBoardingInfo = TestUtils.mockInstance(new OnBoardingInfo());
        BillingData billingData = TestUtils.mockInstance(new BillingData());
        OnboardingResponseData onboardingData = TestUtils.mockInstance(new OnboardingResponseData());
        onboardingData.setInstitutionId(institutionId);
        onboardingData.setBilling(billingData);
        onBoardingInfo.setInstitutions(Collections.singletonList(onboardingData));
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any(), Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = partyConnector.getOnboardedInstitution(institutionId);
        // then
        assertNotNull(institutionInfo);
        assertEquals(onboardingData.getDescription(), institutionInfo.getDescription());
        assertEquals(onboardingData.getDigitalAddress(), institutionInfo.getDigitalAddress());
        assertEquals(onboardingData.getInstitutionId(), institutionInfo.getInstitutionId());
        assertEquals(onboardingData.getState().toString(), institutionInfo.getStatus());
        assertEquals(onboardingData.getAddress(), institutionInfo.getAddress());
        assertEquals(onboardingData.getBilling().getRecipientCode(), institutionInfo.getBilling().getRecipientCode());
        assertEquals(onboardingData.getBilling().isPublicService(), institutionInfo.getBilling().isPublicService());
        assertEquals(onboardingData.getBilling().getVatNumber(), institutionInfo.getBilling().getVatNumber());

        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(institutionId, EnumSet.of(ACTIVE));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getOnboardedInstitution_emptyInstitutions() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        onBoardingInfo.setInstitutions(Collections.emptyList());
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any(), Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = partyConnector.getOnboardedInstitution(institutionId);
        // then
        assertNull(institutionInfo);
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(institutionId, EnumSet.of(ACTIVE));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getOnboardedInstitution_nullOnBoardingInfo() {
        // given
        String institutionId = "institutionId";
        // when
        InstitutionInfo institutionInfo = partyConnector.getOnboardedInstitution(institutionId);
        // then
        assertNull(institutionInfo);
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(institutionId, EnumSet.of(ACTIVE));
        Mockito.verifyNoMoreInteractions(restClientMock);
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
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any(), Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = partyConnector.getOnboardedInstitution(institutionId);
        // then
        assertNull(institutionInfo);
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(institutionId, EnumSet.of(ACTIVE));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


}