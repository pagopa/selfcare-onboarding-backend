package it.pagopa.selfcare.onboarding.connector;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipInfo;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipState;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipsResponse;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.*;
import it.pagopa.selfcare.onboarding.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnBoardingInfo;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnboardingRequest;
import it.pagopa.selfcare.onboarding.connector.rest.model.PersonInfo;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.onboarding.connector.model.RelationshipState.ACTIVE;
import static org.junit.jupiter.api.Assertions.*;

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
        onboardingData2.setState(RelationshipState.PENDING);
        onboardingData2.setInstitutionId(onboardingData1.getInstitutionId());
        OnboardingResponseData onboardingData3 = TestUtils.mockInstance(new OnboardingResponseData(), 3, "setState");
        onboardingData3.setState(RelationshipState.PENDING);
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
        institutionInfos = map.get(RelationshipState.PENDING.name());
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
        Assertions.assertEquals("An Institution id is required", e.getMessage());
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
        Assertions.assertEquals("A productId is required", e.getMessage());
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
        Assertions.assertEquals("An Institution id is required", e.getMessage());
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
        userInfoFilter.setRole(Optional.of(PartyRole.MANAGER));
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
        userInfoFilter.setRole(Optional.of(PartyRole.MANAGER));

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

    @Data
    private static class DummyUserInfo implements UserInfoOperations {
        private String name;
        private String surname;
        private String taxCode;
        private PartyRole role;
        private String email;
        private String productRole;
    }

}