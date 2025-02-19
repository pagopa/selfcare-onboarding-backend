package it.pagopa.selfcare.onboarding.connector;

import static it.pagopa.selfcare.commons.utils.TestUtils.*;
import static it.pagopa.selfcare.onboarding.connector.PartyConnectorImpl.*;
import static it.pagopa.selfcare.onboarding.connector.model.RelationshipState.ACTIVE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipInfo;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipState;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipsResponse;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.institutions.OnboardingResource;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.*;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionUpdate;
import it.pagopa.selfcare.onboarding.connector.rest.client.*;
import it.pagopa.selfcare.onboarding.connector.rest.mapper.InstitutionMapper;
import it.pagopa.selfcare.onboarding.connector.rest.mapper.InstitutionMapperImpl;
import it.pagopa.selfcare.onboarding.connector.rest.model.*;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.InstitutionType;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.OnboardingGet;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.OnboardingGetResponse;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.UserResponse;
import it.pagopa.selfcare.product.entity.Product;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class PartyConnectorImplTest {

    @InjectMocks
    private PartyConnectorImpl partyConnector;

    @Mock
    private PartyProcessRestClient restClientMock;

    @Mock
    private MsOnboardingApiClient onboardingApiClient;


    @Spy
    private InstitutionMapper institutionMapper = new InstitutionMapperImpl();

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
        InstitutionUpdate institutionUpdate = mockInstance(new InstitutionUpdate());
        institutionUpdate.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        onboardingData.setBilling(billing);
        onboardingData.setInstitutionUpdate(institutionUpdate);

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
    void onboardingOrganization_nullGeographicTaxonomies() {
        // given
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Billing billing = mockInstance(new Billing());
        InstitutionUpdate institutionUpdate = mockInstance(new InstitutionUpdate());
        institutionUpdate.setGeographicTaxonomies(null);
        onboardingData.setBilling(billing);
        onboardingData.setUsers(List.of(mockInstance(new User())));
        onboardingData.setInstitutionUpdate(institutionUpdate);
        // when
        partyConnector.onboardingOrganization(onboardingData);
        // then
        verify(restClientMock, times(1))
                .onboardingOrganization(onboardingRequestCaptor.capture());
        OnboardingInstitutionRequest request = onboardingRequestCaptor.getValue();
        assertEquals(onboardingData.getInstitutionExternalId(), request.getInstitutionExternalId());
        assertNull(request.getInstitutionUpdate().getGeographicTaxonomyCodes());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void onboardingOrganization() {
        // given
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        Billing billing = mockInstance(new Billing());
        InstitutionUpdate institutionUpdate = mockInstance(new InstitutionUpdate());
        institutionUpdate.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
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
        assertEquals(1, request.getInstitutionUpdate().getGeographicTaxonomyCodes().size());
        assertEquals(onboardingData.getProductId(), request.getProductId());
        assertEquals(onboardingData.getProductName(), request.getProductName());
        assertEquals(onboardingData.getUsers().get(0).getName(), request.getUsers().get(0).getName());
        assertEquals(onboardingData.getUsers().get(0).getSurname(), request.getUsers().get(0).getSurname());
        assertEquals(onboardingData.getUsers().get(0).getTaxCode(), request.getUsers().get(0).getTaxCode());
        assertEquals(onboardingData.getUsers().get(0).getRole(), request.getUsers().get(0).getRole());
        assertEquals(onboardingData.getUsers().get(0).getEmail(), request.getUsers().get(0).getEmail());
        assertEquals(onboardingData.getInstitutionUpdate().getGeographicTaxonomies().get(0).getCode(), request.getInstitutionUpdate().getGeographicTaxonomyCodes().get(0));
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void onboardOrganization_nullLocation() {
        // given
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setLocation");
        Billing billing = mockInstance(new Billing());
        InstitutionUpdate institutionUpdate = mockInstance(new InstitutionUpdate());
        institutionUpdate.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
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
        assertNull(request.getInstitutionUpdate().getCity());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getOnboardedInstitutions_WithChildProduct() {
        // given
        final String userId = "userId";
        final String productFilter = "prod-io";
        final String childProductId = "prod-io-premium";

        Product product = new Product();
        product.setId(childProductId);
        product.setParentId(productFilter);

        it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.InstitutionResponse institutionResponse = new it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.InstitutionResponse();
        institutionResponse.setId("id");
        institutionResponse.setDescription("description");

        it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.InstitutionResponse institutionResponse1 = new it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.InstitutionResponse();
        institutionResponse1.setId("id1");
        institutionResponse1.setDescription("description");
        institutionResponse1.setInstitutionType(InstitutionType.PA.name());
        OnboardingGet onboardingData1 = new OnboardingGet();
        onboardingData1.setInstitution(institutionResponse1);
        onboardingData1.setProductId("prod-io");

        OnboardingGet onboardingData2 = new OnboardingGet();
        it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.InstitutionResponse institutionResponse2 = new it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.InstitutionResponse();
        institutionResponse2.setId("id2");
        institutionResponse2.setDescription("description");
        institutionResponse2.setInstitutionType(InstitutionType.PA.name());
        onboardingData2.setInstitution(institutionResponse2);
        onboardingData2.setProductId("prod-io");
        UserResponse userResponse = new UserResponse();
        userResponse.setId(userId);
        userResponse.setRole(it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.PartyRole.MANAGER);
        onboardingData2.setUsers(List.of(userResponse));

        OnboardingGet onboardingData3 = new OnboardingGet();
        it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.InstitutionResponse institutionResponse3 = new it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.InstitutionResponse();
        institutionResponse3.setId("id3");
        institutionResponse3.setDescription("description");
        institutionResponse3.setInstitutionType(InstitutionType.PA.name());
        onboardingData3.setInstitution(institutionResponse3);
        onboardingData3.setProductId("prod-io");

        OnboardingGetResponse onboardingGetResponse = new OnboardingGetResponse();
        onboardingGetResponse.setItems(List.of(onboardingData1, onboardingData2, onboardingData3));
        ResponseEntity<OnboardingGetResponse> responseEntity = mock(ResponseEntity.class);

        // when
        when(responseEntity.getBody()).thenReturn(onboardingGetResponse);
        when(onboardingApiClient._getOnboardingWithFilter(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(),
                any(), any(), any()))
                .thenReturn(responseEntity);


        Collection<InstitutionInfo> institutions = partyConnector.getInstitutionsByUser(product, userId);
        // then
        assertNotNull(institutions);
        assertEquals(3, institutions.size());
        verify(onboardingApiClient, times(1))
                ._getOnboardingWithFilter(any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any(),
                        any(), any(), any());
        verifyNoMoreInteractions(onboardingApiClient);
    }

    @Test
    void getOnboardedInstitutions() {
        // given
        final String userId = "userId";
        final String productFilter = "prod-io";
        final String childProductId = "prod-io-premium";

        Product product = new Product();
        product.setId(childProductId);
        product.setParentId(productFilter);

        it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.InstitutionResponse institutionResponse1 = new it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.InstitutionResponse();
        institutionResponse1.setId("id");
        institutionResponse1.setDescription("description");
        institutionResponse1.setInstitutionType(InstitutionType.PA.name());
        OnboardingGet onboardingData1 = new OnboardingGet();
        onboardingData1.setInstitution(institutionResponse1);
        onboardingData1.setProductId("prod-io");

        OnboardingGet onboardingData2 = new OnboardingGet();
        it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.InstitutionResponse institutionResponse2 = new it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.InstitutionResponse();
        institutionResponse2.setId("id");
        institutionResponse2.setDescription("description");
        institutionResponse2.setInstitutionType(InstitutionType.PA.name());
        onboardingData2.setInstitution(institutionResponse2);
        onboardingData2.setProductId("prod-io");

        OnboardingGetResponse onboardingGetResponse = new OnboardingGetResponse();
        onboardingGetResponse.setItems(List.of(onboardingData1, onboardingData2));
        ResponseEntity<OnboardingGetResponse> responseEntity = mock(ResponseEntity.class);

        when(responseEntity.getBody()).thenReturn(onboardingGetResponse);
        when(onboardingApiClient._getOnboardingWithFilter(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(),
                any(), any(), any()))
                .thenReturn(responseEntity);

        // when
        Collection<InstitutionInfo> institutions = partyConnector.getInstitutionsByUser(product, userId);
        // then
        assertNotNull(institutions);
        assertEquals(0, institutions.size());
        verify(onboardingApiClient, times(1))
                ._getOnboardingWithFilter(any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any(),
                        any(), any(), any());
        verifyNoMoreInteractions(onboardingApiClient);
    }

    @Test
    void getOnboardedInstitutions_OnlyParent() {
        // given
        final String userId = "userId";
        final String productFilter = "prod-io";

        Product product = new Product();
        product.setId(productFilter);
        product.setInstitutionTypesAllowed(List.of(InstitutionType.PA.name()));

        OnboardingGet onboardingData = new OnboardingGet();
        it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.InstitutionResponse institutionResponse = new it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.InstitutionResponse();
        institutionResponse.setId("id");
        institutionResponse.setDescription("description");
        institutionResponse.setInstitutionType(InstitutionType.PA.name());
        onboardingData.setInstitution(institutionResponse);
        onboardingData.setProductId("prod-io");

        OnboardingGetResponse onboardingGetResponse = new OnboardingGetResponse();
        onboardingGetResponse.setItems(List.of(onboardingData));
        ResponseEntity<OnboardingGetResponse> responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getBody()).thenReturn(onboardingGetResponse);

        when(onboardingApiClient._getOnboardingWithFilter(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(),
                any(), any(), any()))
                .thenReturn(responseEntity);

        // when
        Collection<InstitutionInfo> institutions = partyConnector.getInstitutionsByUser(product, userId);
        // then
        assertNotNull(institutions);
        assertEquals(1, institutions.size());
        verify(onboardingApiClient, times(1))
                ._getOnboardingWithFilter(any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any(),
                        any(), any(), any());
        verifyNoMoreInteractions(onboardingApiClient);
    }

    @Test
    void getOnboardedInstitutions_nullInstitutions() {
        //given
        Product product = new Product();
        product.setId("prod-io");
        OnboardingGetResponse onboardingGetResponse = new OnboardingGetResponse();
        onboardingGetResponse.setItems(List.of());
        ResponseEntity<OnboardingGetResponse> responseEntity = mock(ResponseEntity.class);

        // when
        when(responseEntity.getBody()).thenReturn(onboardingGetResponse);
        when(onboardingApiClient._getOnboardingWithFilter(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(),
                any(), any(), any()))
                .thenReturn(responseEntity);
        Collection<InstitutionInfo> institutionInfos =
                partyConnector.getInstitutionsByUser(product, null);

        //then
        assertNotNull(institutionInfos);
        assertTrue(institutionInfos.isEmpty());
        verify(onboardingApiClient, times(1))
                ._getOnboardingWithFilter(any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any(),
                        any(), any(), any());
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
        assertEquals(REQUIRED_INSTITUTION_EXTERNAL_ID_MESSAGE, e.getMessage());
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
        assertEquals(REQUIRED_INSTITUTION_EXTERNAL_ID_MESSAGE, e.getMessage());
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
        InstitutionResponse institutionResponseMock = mockInstance(new InstitutionResponse());
        institutionResponseMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        when(restClientMock.getInstitutionByExternalId(anyString()))
                .thenReturn(institutionResponseMock);
        //when
        Institution institution = partyConnector.getInstitutionByExternalId(institutionId);
        //then
        assertNotNull(institution);
        assertEquals(institutionResponseMock.getExternalId(), institution.getExternalId());
        assertEquals(institutionResponseMock.getDescription(), institution.getDescription());
        assertEquals(institutionResponseMock.getAddress(), institution.getAddress());
        assertEquals(institutionResponseMock.getTaxCode(), institution.getTaxCode());
        assertEquals(institutionResponseMock.getId(), institution.getId());
        assertEquals(institutionResponseMock.getZipCode(), institution.getZipCode());
        assertEquals(institutionResponseMock.getDigitalAddress(), institution.getDigitalAddress());
        assertEquals(institutionResponseMock.getInstitutionType(), institution.getInstitutionType());
        assertEquals(institutionResponseMock.getGeographicTaxonomies().get(0).getCode(), institution.getGeographicTaxonomies().get(0).getCode());
        assertEquals(institutionResponseMock.getGeographicTaxonomies().get(0).getDesc(), institution.getGeographicTaxonomies().get(0).getDesc());
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
        assertEquals(REQUIRED_INSTITUTION_EXTERNAL_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void getInstitutionById_nullInstitutionId() {
        //given
        String institutionId = null;
        //when
        Executable exe = () -> partyConnector.getInstitutionById(institutionId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, exe);
        assertEquals(REQUIRED_INSTITUTION_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }


    @Test
    void getInstitutionById() {
        //given
        String institutionId = "institutionId";
        InstitutionResponse institutionResponseMock = mockInstance(new InstitutionResponse());
        institutionResponseMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        when(restClientMock.getInstitutionById(institutionId))
                .thenReturn(institutionResponseMock);
        //when
        Institution institution = partyConnector.getInstitutionById(institutionId);
        //then
        assertNotNull(institution);
        assertEquals(institutionResponseMock.getExternalId(), institution.getExternalId());
        assertEquals(institutionResponseMock.getDescription(), institution.getDescription());
        assertEquals(institutionResponseMock.getAddress(), institution.getAddress());
        assertEquals(institutionResponseMock.getTaxCode(), institution.getTaxCode());
        assertEquals(institutionResponseMock.getId(), institution.getId());
        assertEquals(institutionResponseMock.getZipCode(), institution.getZipCode());
        assertEquals(institutionResponseMock.getDigitalAddress(), institution.getDigitalAddress());
        assertEquals(institutionResponseMock.getInstitutionType(), institution.getInstitutionType());
        assertEquals(institutionResponseMock.getGeographicTaxonomies().get(0).getCode(), institution.getGeographicTaxonomies().get(0).getCode());
        assertEquals(institutionResponseMock.getGeographicTaxonomies().get(0).getDesc(), institution.getGeographicTaxonomies().get(0).getDesc());
        verify(restClientMock, times(1))
                .getInstitutionById(institutionId);
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getOnboardings_emptyOnboardings() {
        // given
        String institutionId = "institutionId";
        OnboardingsResponse onboardingsResponse = new OnboardingsResponse();
        onboardingsResponse.setOnboardings(Collections.emptyList());
        when(restClientMock.getOnboardings(any(), any()))
                .thenReturn(onboardingsResponse);
        // when
        List<OnboardingResource> onboardings = partyConnector.getOnboardings(institutionId, null);
        // then
        assertTrue(onboardings.isEmpty());
        verify(restClientMock, times(1))
                .getOnboardings(institutionId, null);
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getOnboardedInstitution_nullInstitutionId() {
        //given
        String institutionId = null;
        //when
        Executable executable = () -> partyConnector.getOnboardings(institutionId, null);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_ID_MESSAGE, e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void createInstitutionUsingExternalId_nullId() {
        //given
        String externalId = null;
        //when
        Executable executable = () -> partyConnector.createInstitutionUsingExternalId(externalId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_EXTERNAL_ID_MESSAGE, e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @Test
    void createInstitutionUsingExternalId() {
        //given
        String externalId = "externalId";
        InstitutionResponse institutionResponse = mockInstance(new InstitutionResponse());
        when(restClientMock.createInstitutionUsingExternalId(anyString()))
                .thenReturn(institutionResponse);
        //when
        Institution result = partyConnector.createInstitutionUsingExternalId(externalId);
        //then
        assertNotNull(result);
        reflectionEqualsByName(institutionResponse, result);
        assertEquals(institutionResponse.getRea(), result.getCompanyInformations().getRea());
        assertEquals(institutionResponse.getShareCapital(), result.getCompanyInformations().getShareCapital());
        assertEquals(institutionResponse.getBusinessRegisterPlace(), result.getCompanyInformations().getBusinessRegisterPlace());
        assertEquals(institutionResponse.getSupportEmail(), result.getAssistanceContacts().getSupportEmail());
        assertEquals(institutionResponse.getSupportPhone(), result.getAssistanceContacts().getSupportPhone());
        verify(restClientMock, times(1))
                .createInstitutionUsingExternalId(externalId);
    }

    @Test
    void createInstitution() {
        //given
        final OnboardingData onboardingData = mockInstance(new OnboardingData());
        InstitutionResponse institutionResponse = mockInstance(new InstitutionResponse());
        List<GeographicTaxonomy> geographicTaxonomyList = List.of(mockInstance(new GeographicTaxonomy()));
        InstitutionUpdate institutionUpdate = mockInstance(new InstitutionUpdate());
        institutionUpdate.setGeographicTaxonomies(geographicTaxonomyList);
        institutionResponse.setGeographicTaxonomies(geographicTaxonomyList);
        when(restClientMock.createInstitution(any()))
                .thenReturn(institutionResponse);
        //when
        Institution result = partyConnector.createInstitution(onboardingData);
        //then
        assertNotNull(result);
        reflectionEqualsByName(institutionResponse, result);
        assertEquals(institutionResponse.getRea(), result.getCompanyInformations().getRea());
        assertEquals(institutionResponse.getShareCapital(), result.getCompanyInformations().getShareCapital());
        assertEquals(institutionResponse.getBusinessRegisterPlace(), result.getCompanyInformations().getBusinessRegisterPlace());
        assertEquals(institutionResponse.getSupportEmail(), result.getAssistanceContacts().getSupportEmail());
        assertEquals(institutionResponse.getSupportPhone(), result.getAssistanceContacts().getSupportPhone());
        final ArgumentCaptor<InstitutionSeed> argumentCaptor = ArgumentCaptor.forClass(InstitutionSeed.class);
        verify(restClientMock, times(1))
                .createInstitution(argumentCaptor.capture());
        final InstitutionSeed institutionSeed = argumentCaptor.getValue();
        assertEquals(onboardingData.getInstitutionUpdate().getDescription(), institutionSeed.getDescription());
        assertEquals(onboardingData.getInstitutionUpdate().getDigitalAddress(), institutionSeed.getDigitalAddress());
        assertEquals(onboardingData.getInstitutionUpdate().getAddress(), institutionSeed.getAddress());
        assertEquals(onboardingData.getInstitutionUpdate().getZipCode(), institutionSeed.getZipCode());
        assertEquals(onboardingData.getInstitutionUpdate().getTaxCode(), institutionSeed.getTaxCode());
        assertEquals(onboardingData.getInstitutionType(), institutionSeed.getInstitutionType());
        assertTrue(institutionSeed.getAttributes().isEmpty());
        assertEquals(onboardingData.getInstitutionUpdate().getPaymentServiceProvider(), institutionSeed.getPaymentServiceProvider());
        assertEquals(onboardingData.getInstitutionUpdate().getDataProtectionOfficer(), institutionSeed.getDataProtectionOfficer());
        assertEquals(onboardingData.getInstitutionUpdate().getGeographicTaxonomies(), institutionSeed.getGeographicTaxonomies());
        assertEquals(onboardingData.getInstitutionUpdate().getRea(), institutionSeed.getRea());
        assertEquals(onboardingData.getInstitutionUpdate().getShareCapital(), institutionSeed.getShareCapital());
        assertEquals(onboardingData.getInstitutionUpdate().getBusinessRegisterPlace(), institutionSeed.getBusinessRegisterPlace());
        assertEquals(onboardingData.getInstitutionUpdate().getSupportEmail(), institutionSeed.getSupportEmail());
        assertEquals(onboardingData.getInstitutionUpdate().getSupportPhone(), institutionSeed.getSupportPhone());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void createInstitution_nullLocation() {
        //given
        final OnboardingData onboardingData = mockInstance(new OnboardingData());
        onboardingData.setLocation(null);
        InstitutionResponse institutionResponse = mockInstance(new InstitutionResponse());
        List<GeographicTaxonomy> geographicTaxonomyList = List.of(mockInstance(new GeographicTaxonomy()));
        InstitutionUpdate institutionUpdate = mockInstance(new InstitutionUpdate());
        institutionUpdate.setGeographicTaxonomies(geographicTaxonomyList);
        institutionResponse.setGeographicTaxonomies(geographicTaxonomyList);
        when(restClientMock.createInstitution(any()))
                .thenReturn(institutionResponse);
        //when
        Institution result = partyConnector.createInstitution(onboardingData);
        //then
        assertNotNull(result);
        reflectionEqualsByName(institutionResponse, result);
        assertEquals(institutionResponse.getRea(), result.getCompanyInformations().getRea());
        assertEquals(institutionResponse.getShareCapital(), result.getCompanyInformations().getShareCapital());
        assertEquals(institutionResponse.getBusinessRegisterPlace(), result.getCompanyInformations().getBusinessRegisterPlace());
        assertEquals(institutionResponse.getSupportEmail(), result.getAssistanceContacts().getSupportEmail());
        assertEquals(institutionResponse.getSupportPhone(), result.getAssistanceContacts().getSupportPhone());
        final ArgumentCaptor<InstitutionSeed> argumentCaptor = ArgumentCaptor.forClass(InstitutionSeed.class);
        verify(restClientMock, times(1))
                .createInstitution(argumentCaptor.capture());
        final InstitutionSeed institutionSeed = argumentCaptor.getValue();
        assertEquals(onboardingData.getInstitutionUpdate().getDescription(), institutionSeed.getDescription());
        assertEquals(onboardingData.getInstitutionUpdate().getDigitalAddress(), institutionSeed.getDigitalAddress());
        assertEquals(onboardingData.getInstitutionUpdate().getAddress(), institutionSeed.getAddress());
        assertEquals(onboardingData.getInstitutionUpdate().getZipCode(), institutionSeed.getZipCode());
        assertEquals(onboardingData.getInstitutionUpdate().getTaxCode(), institutionSeed.getTaxCode());
        assertEquals(onboardingData.getInstitutionType(), institutionSeed.getInstitutionType());
        assertTrue(institutionSeed.getAttributes().isEmpty());
        assertEquals(onboardingData.getInstitutionUpdate().getPaymentServiceProvider(), institutionSeed.getPaymentServiceProvider());
        assertEquals(onboardingData.getInstitutionUpdate().getDataProtectionOfficer(), institutionSeed.getDataProtectionOfficer());
        assertEquals(onboardingData.getInstitutionUpdate().getGeographicTaxonomies(), institutionSeed.getGeographicTaxonomies());
        assertEquals(onboardingData.getInstitutionUpdate().getRea(), institutionSeed.getRea());
        assertEquals(onboardingData.getInstitutionUpdate().getShareCapital(), institutionSeed.getShareCapital());
        assertEquals(onboardingData.getInstitutionUpdate().getBusinessRegisterPlace(), institutionSeed.getBusinessRegisterPlace());
        assertEquals(onboardingData.getInstitutionUpdate().getSupportEmail(), institutionSeed.getSupportEmail());
        assertEquals(onboardingData.getInstitutionUpdate().getSupportPhone(), institutionSeed.getSupportPhone());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getInstitutionManager_nullInstitutionId() {
        //given
        String institutionId = null;
        String productId = "productId";
        //when
        Executable executable = () -> partyConnector.getInstitutionManager(institutionId, productId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An Institution external id is required", e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @Test
    void getInstitutionManager_nullProductId() {
        //given
        String institutionId = "institutionId";
        String productId = null;
        //when
        Executable executable = () -> partyConnector.getInstitutionManager(institutionId, productId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A product Id is required", e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @Test
    void getInstitutionManager() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        RelationshipInfo relationshipInfo = mockInstance(new RelationshipInfo());
        when(restClientMock.getInstitutionManager(anyString(), anyString()))
                .thenReturn(relationshipInfo);
        //when
        UserInfo userInfo = partyConnector.getInstitutionManager(institutionId, productId);
        //then
        checkNotNullFields(userInfo, "user");
        verify(restClientMock, times(1))
                .getInstitutionManager(institutionId, productId);

    }

    @Test
    void getInstitutionBillingData_nullExternalId() {
        //given
        String externalId = null;
        String productId = "productId";
        //when
        Executable executable = () -> partyConnector.getInstitutionBillingData(externalId, productId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_EXTERNAL_ID_MESSAGE, e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @Test
    void getInstitutionBillingData_nullProductId() {
        //given
        String externalId = "externalId";
        String productId = null;
        //when
        Executable executable = () -> partyConnector.getInstitutionBillingData(externalId, productId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_PRODUCT_ID_MESSAGE, e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @Test
    void getInstitutionBillingData() {
        //given
        String externalId = "externalId";
        String productId = "productId";
        BillingDataResponse billingDataResponseMock = mockInstance(new BillingDataResponse());
        Billing billingMock = mockInstance(new Billing());
        billingDataResponseMock.setBilling(billingMock);

        when(restClientMock.getInstitutionBillingData(anyString(), anyString()))
                .thenReturn(billingDataResponseMock);
        //when
        InstitutionInfo institutionInfo = partyConnector.getInstitutionBillingData(externalId, productId);
        //then
        assertNotNull(institutionInfo);
        checkNotNullFields(institutionInfo, "status", "category", "userRole", "institutionLocation", "paymentServiceProvider", "gpuData", "dataProtectionOfficer");
        reflectionEqualsByName(billingDataResponseMock, institutionInfo);
        verify(restClientMock, times(1))
                .getInstitutionBillingData(externalId, productId);
        verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void verifyOnboarding_nullExternalInstitutionId() {
        // given
        final String externalInstitutionId = null;
        final String productId = "productId";
        // when
        final Executable executable = () -> partyConnector.verifyOnboarding(externalInstitutionId, productId);
        // then
        final Exception e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An Institution external id is required", e.getMessage());
        verifyNoInteractions(restClientMock);
    }


    @Test
    void verifyOnboarding_nullProductId() {
        // given
        final String externalInstitutionId = "externalInstitutionId";
        final String productId = null;
        // when
        final Executable executable = () -> partyConnector.verifyOnboarding(externalInstitutionId, productId);
        // then
        final Exception e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A product Id is required", e.getMessage());
        verifyNoInteractions(restClientMock);
    }


    @Test
    void verifyOnboarding() {
        // given
        final String externalInstitutionId = "externalInstitutionId";
        final String productId = "productId";
        // when
        final Executable executable = () -> partyConnector.verifyOnboarding(externalInstitutionId, productId);
        // then
        assertDoesNotThrow(executable);
        verify(restClientMock, times(1))
                .verifyOnboarding(externalInstitutionId, productId);
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void verifyOnboardingSubunitCode() {
        // given
        final String taxCode = "taxCode";
        final String subunitCode = "subunitCode";
        final String productId = "productId";
        final String externalId = "externalId";
        final String origin = "origin";
        final String originId = "originId";
        // when
        final Executable executable = () -> partyConnector.verifyOnboarding(productId, externalId, taxCode, origin, originId, subunitCode);
        // then
        assertDoesNotThrow(executable);
        verify(restClientMock, times(1))
                ._verifyOnboardingInfoByFiltersUsingHEAD(productId, externalId, taxCode, origin, originId, subunitCode);
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getInstitutionsByTaxCodeAndSubunitCode_nullTaxCode() {
        // given
        final String taxCode = null;
        // when
        final Executable executable = () -> partyConnector.getInstitutionsByTaxCodeAndSubunitCode(taxCode, null);
        // then
        final Exception e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_TAXCODE_MESSAGE, e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @Test
    void getInstitutionsByTaxCodeAndSubunitCode() {
        // given
        final String taxCode = "taxCode";
        final String subunitCode = "subunitCode";
        InstitutionsResponse institutionsResponse = new InstitutionsResponse();
        institutionsResponse.setInstitutions(List.of());
        when(restClientMock.getInstitutions(anyString(), anyString())).thenReturn(institutionsResponse);
        // when
        final Executable executable = () -> partyConnector.getInstitutionsByTaxCodeAndSubunitCode(taxCode, subunitCode);
        // then
        assertDoesNotThrow(executable);
        verify(restClientMock, times(1))
                .getInstitutions(taxCode, subunitCode);
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void createInstitutionFromIpa_nullTaxCode() {
        // given
        final String taxCode = null;
        // when
        final Executable executable = () -> partyConnector.createInstitutionFromIpa(taxCode, null, null);
        // then
        final Exception e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_TAXCODE_MESSAGE, e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @Test
    void createInstitutionFromAnac() {
        // given
        OnboardingData onboardingData = new OnboardingData();
        onboardingData.setInstitutionUpdate(new InstitutionUpdate());
        onboardingData.setLocation(new InstitutionLocation());
        onboardingData.setTaxCode("taxCode");
        // when
        final Executable executable = () -> partyConnector.createInstitutionFromANAC(onboardingData);
        assertDoesNotThrow(executable);
        verify(restClientMock, times(1))
                .createInstitutionFromANAC(any());
        verifyNoMoreInteractions(restClientMock);

    }

    @Test
    void createInstitutionFromIvass() {
        // given
        OnboardingData onboardingData = new OnboardingData();
        onboardingData.setInstitutionUpdate(new InstitutionUpdate());
        onboardingData.setLocation(new InstitutionLocation());
        onboardingData.setTaxCode("taxCode");
        // when
        final Executable executable = () -> partyConnector.createInstitutionFromIVASS(onboardingData);
        assertDoesNotThrow(executable);
        verify(restClientMock, times(1))
                .createInstitutionFromIVASS(any());
        verifyNoMoreInteractions(restClientMock);

    }

    @Test
    void createInstitutionFromInfocamere() {
        // given
        OnboardingData onboardingData = new OnboardingData();
        onboardingData.setInstitutionUpdate(new InstitutionUpdate());
        onboardingData.setTaxCode("taxCode");
        // when
        final Executable executable = () -> partyConnector.createInstitutionFromInfocamere(onboardingData);
        assertDoesNotThrow(executable);
        verify(restClientMock, times(1))
                .createInstitutionFromInfocamere(any());
        verifyNoMoreInteractions(restClientMock);

    }
}