package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.BillingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
import it.pagopa.selfcare.onboarding.web.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        assertEquals(billingDataDto.getBusinessName(), resource.getBillingData().getDescription());
        assertEquals(billingDataDto.getPublicService(), resource.getBillingData().isPublicService());
        assertEquals(billingDataDto.getRegisteredOffice(), resource.getBillingData().getPhysicalAddress());
        assertEquals(billingDataDto.getDigitalAddress(), resource.getBillingData().getDigitalAddress());
        assertEquals(billingDataDto.getTaxCode(), resource.getBillingData().getTaxCode());
        assertEquals(billingDataDto.getVatNumber(), resource.getBillingData().getVatNumber());
        assertEquals(billingDataDto.getRecipientCode(), resource.getBillingData().getRecipientCode());
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
    void toDto() {
        //given
        UserInfo model = TestUtils.mockInstance(new UserInfo());
        //when
        UserDto resource = OnboardingMapper.toDto(model);
        //then
        assertNotNull(resource);
        TestUtils.reflectionEqualsByName(resource, model);
    }

    @Test
    void toDto_null() {
        //given
        //when
        UserDto resource = OnboardingMapper.toDto(null);
        //then
        assertNull(resource);
    }

    @Test
    void toResource_institutionInfo() {
        //given
        InstitutionInfo model = TestUtils.mockInstance(new InstitutionInfo());
        //when
        InstitutionResource resource = OnboardingMapper.toResource(model);
        //then
        assertNotNull(resource);
        assertEquals(resource.getExternalId(), model.getInstitutionId());
        assertEquals(resource.getDescription(), model.getDescription());
        TestUtils.reflectionEqualsByName(model, resource);
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
        UserInfo model = TestUtils.mockInstance(new UserInfo());
        //when
        UserResource resource = OnboardingMapper.toResource(model);
        //then
        assertNotNull(resource);
        TestUtils.reflectionEqualsByName(resource, model);
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
        BillingData resource = OnboardingMapper.fromDto(model);
        //then
        assertNotNull(resource);
        TestUtils.reflectionEqualsByName(model, resource);
    }

    @Test
    void fromDto_null() {
        //given
        //when
        BillingData resource = OnboardingMapper.fromDto(null);
        //then
        assertNull(resource);
    }

}