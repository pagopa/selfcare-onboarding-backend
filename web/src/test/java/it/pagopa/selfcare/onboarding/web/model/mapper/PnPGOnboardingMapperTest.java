package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.PnPGOnboardingData;
import it.pagopa.selfcare.onboarding.web.model.PnPGBillingDataDto;
import it.pagopa.selfcare.onboarding.web.model.PnPGOnboardingDto;
import it.pagopa.selfcare.onboarding.web.model.UserDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.commons.utils.TestUtils.reflectionEqualsByName;
import static org.junit.jupiter.api.Assertions.*;

class PnPGOnboardingMapperTest {

    @Test
    void toOnboardingData() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        UserDto userDto = mockInstance(new UserDto(), "setProductRole");
        userDto.setProductRole("ADMIN");
        List<UserDto> userDtos = List.of(userDto);
        PnPGBillingDataDto pnPGBillingDataDto = mockInstance(new PnPGBillingDataDto(), "setTaxCode");
        pnPGBillingDataDto.setTaxCode(institutionId);
        PnPGOnboardingDto model = mockInstance(new PnPGOnboardingDto(), "setUsers", "setBillingData");
        model.setUsers(userDtos);
        model.setBillingData(pnPGBillingDataDto);
        PnPGBillingDataDto billingDataDto = mockInstance(new PnPGBillingDataDto());
        //when
        PnPGOnboardingData resource = PnPGOnboardingMapper.toOnboardingData(institutionId, productId, model);
        //then
        assertNotNull(resource);
        assertEquals(model.getUsers().size(), resource.getUsers().size());
        assertEquals(institutionId, resource.getInstitutionExternalId());
        assertEquals(productId, resource.getProductId());
        reflectionEqualsByName(billingDataDto, resource.getBillingRequest());
        reflectionEqualsByName(userDtos.get(0), resource.getUsers().get(0));
        reflectionEqualsByName(model.getBillingData(), resource.getInstitutionUpdate());
    }

    @Test
    void toOnboardingData_null() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        PnPGOnboardingDto onboardingDto = null;
        //when
        PnPGOnboardingData resource = PnPGOnboardingMapper.toOnboardingData(institutionId, productId, onboardingDto);
        //then
        assertNull(resource);
    }

}