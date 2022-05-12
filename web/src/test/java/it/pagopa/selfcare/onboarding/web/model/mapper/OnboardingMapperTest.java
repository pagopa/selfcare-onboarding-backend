package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.Billing;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.web.model.BillingDataDto;
import it.pagopa.selfcare.onboarding.web.model.OnboardingDto;
import it.pagopa.selfcare.onboarding.web.model.UserDto;
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