package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.Billing;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
import it.pagopa.selfcare.onboarding.web.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;

class OnboardingMapperTest {

    @Test
    void toOnboardingData() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        List<UserDto> userDtos = List.of(TestUtils.mockInstance(new UserDto()));
        OnboardingDto model = TestUtils.mockInstance(new OnboardingDto());
        BillingDataDto billingDataDto = TestUtils.mockInstance(new BillingDataDto());
        model.setBillingData(billingDataDto);
        model.setUsers(userDtos);
        //when
        OnboardingData resource = OnboardingMapper.toOnboardingData(institutionId, productId, model);
        //then
        assertNotNull(resource);
        assertEquals(1, model.getUsers().size());
        assertEquals(institutionId, resource.getInstitutionId());
        assertEquals(productId, resource.getProductId());
        assertEquals(billingDataDto.getPublicServices(), resource.getBilling().getPublicServices());
        assertEquals(billingDataDto.getVatNumber(), resource.getBilling().getVatNumber());
        assertEquals(billingDataDto.getRecipientCode(), resource.getBilling().getRecipientCode());
        assertEquals(userDtos.get(0).getEmail(), resource.getUsers().get(0).getEmail());
        assertEquals(userDtos.get(0).getName(), resource.getUsers().get(0).getName());
        assertEquals(userDtos.get(0).getSurname(), resource.getUsers().get(0).getSurname());
        assertEquals(userDtos.get(0).getTaxCode(), resource.getUsers().get(0).getTaxCode());
        assertEquals(userDtos.get(0).getRole(), resource.getUsers().get(0).getRole());
        assertEquals(userDtos.get(0).getProductRole(), resource.getUsers().get(0).getProductRole());
        assertEquals(model.getOrigin(), resource.getOrigin());
        assertEquals(model.getInstitutionType(), resource.getInstitutionType());
    }

    @Test
    void toOnboardingData_null() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        OnboardingDto onboardingDto = null;
        //when
        OnboardingData resource = OnboardingMapper.toOnboardingData(institutionId, productId, onboardingDto);
        //then
        assertNull(resource);
    }

    @Test
    void toUser() {
        //given
        UserDto model = TestUtils.mockInstance(new UserDto());
        //when
        User resource = OnboardingMapper.toUser(model);
        //then
        assertNotNull(resource);
        TestUtils.reflectionEqualsByName(resource, model);
    }

    @Test
    void toUser_null() {
        //given
        //when
        User resource = OnboardingMapper.toUser(null);
        //then
        assertNull(resource);
    }


    @Test
    void toResource_institutionInfo() {
        //given
        InstitutionInfo model = TestUtils.mockInstance(new InstitutionInfo(), "setId");
        model.setId(randomUUID().toString());
        model.setBilling(TestUtils.mockInstance(new Billing()));
        //when
        InstitutionResource resource = OnboardingMapper.toResource(model);
        //then
        assertNotNull(resource);
        assertEquals(resource.getId().toString(), model.getId());
        assertEquals(resource.getExternalId(), model.getExternalId());
        assertEquals(resource.getDescription(), model.getDescription());
        assertEquals(resource.getAddress(), model.getAddress());
        assertEquals(resource.getDigitalAddress(), model.getDigitalAddress());
        assertEquals(resource.getZipCode(), model.getZipCode());
        assertEquals(resource.getTaxCode(), model.getTaxCode());
        assertEquals(resource.getOrigin(), model.getOrigin());

    }

    @Test
    void toResource_nullInstitutionInfo() {
        //given
        //when
        InstitutionResource resource = OnboardingMapper.toResource((InstitutionInfo) null);
        //then
        assertNull(resource);
    }

    @Test
    void toResource_userResource() {
        //given
        UserInfo model = TestUtils.mockInstance(new UserInfo(), "setId", "setInstitutionId");
        model.setId(UUID.randomUUID().toString());
        model.setInstitutionId(UUID.randomUUID().toString());
        //when
        UserResource resource = OnboardingMapper.toResource(model);
        //then
        assertNotNull(resource);
        TestUtils.reflectionEqualsByName(resource, model, "id", "institutionId");
        assertEquals(model.getId(), resource.getId().toString());
        assertEquals(model.getInstitutionId(), resource.getInstitutionId().toString());
    }

    @Test
    void toResource_nullUserResource() {
        //given
        //when
        UserResource resource = OnboardingMapper.toResource((UserInfo) null);
        //then
        assertNull(resource);
    }

    @Test
    void fromDto() {
        //given
        BillingDataDto model = TestUtils.mockInstance(new BillingDataDto());
        //when
        Billing resource = OnboardingMapper.fromDto(model);
        //then
        assertNotNull(resource);
        TestUtils.reflectionEqualsByName(model, resource);
    }

    @Test
    void fromDto_null() {
        //given
        //when
        Billing resource = OnboardingMapper.fromDto(null);
        //then
        assertNull(resource);
    }

}