package it.pagopa.selfcare.onboarding.connector;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipInfo;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipState;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipsResponse;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingResponseData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PartyRole;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
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
                new OnboardingData("institutionId", "productId", Collections.emptyList(), null);
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
                new OnboardingData("institutionId", "productId", List.of(TestUtils.mockInstance(new DummyUserInfo())), null);
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

    @Test
    void getOnboardedInstitutions() {
        //given
        OnBoardingInfo onboardingInfo = new OnBoardingInfo();
        PersonInfo personInfo = TestUtils.mockInstance(new PersonInfo());
        OnboardingResponseData onboardingData = TestUtils.mockInstance(new OnboardingResponseData());
        onboardingInfo.setPerson(personInfo);
        OnboardingResponseData onboardingData2 = TestUtils.mockInstance(new OnboardingResponseData());
        onboardingInfo.setPerson(personInfo);
        onboardingInfo.setInstitutions(List.of(onboardingData, onboardingData2));
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any(), Mockito.any()))
                .thenReturn(onboardingInfo);
        //when
        Collection<InstitutionInfo> institutionInfos = partyConnector.getOnBoardedInstitutions();
        //then
        Assertions.assertNotNull(institutionInfos);
        Assertions.assertEquals(1, institutionInfos.size());
        InstitutionInfo institution = institutionInfos.iterator().next();
        Assertions.assertEquals(onboardingData.getInstitutionId(), institution.getInstitutionId());
        Assertions.assertEquals(onboardingData.getDescription(), institution.getDescription());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(Mockito.isNull(), Mockito.isNotNull());
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
                        Mockito.eq(EnumSet.of(RelationshipState.ACTIVE)),
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
                        Mockito.eq(EnumSet.of(RelationshipState.ACTIVE)),
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
        Assertions.assertEquals("An institutionId is required", e.getMessage());
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

    @Data
    private static class DummyUserInfo implements UserInfo {
        private String name;
        private String surname;
        private String taxCode;
        private PartyRole role;
        private String email;
        private String productRole;
    }

}