package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.InstitutionLegalAddressData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PnPGOnboardingData;
import it.pagopa.selfcare.onboarding.web.model.CompanyOnboardingDto;
import it.pagopa.selfcare.onboarding.web.model.InstitutionLegalAddressResource;
import org.junit.jupiter.api.Test;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.*;

class PnPGOnboardingMapperTest {

//    @Test
//    void toOnboardingData() {
//        //given
//        String institutionId = "institutionId";
//        String productId = "productId";
//        UserDto userDto = mockInstance(new UserDto(), "setProductRole");
//        userDto.setProductRole("");
//        List<UserDto> userDtos = List.of(userDto);
//        PnPGBillingDataDto pnPGBillingDataDto = mockInstance(new PnPGBillingDataDto(), "setTaxCode");
//        pnPGBillingDataDto.setTaxCode(institutionId);
//        PnPGOnboardingDto model = mockInstance(new PnPGOnboardingDto(), "setUsers", "setBillingData");
//        model.setUsers(userDtos);
//        model.setBillingData(pnPGBillingDataDto);
//        PnPGBillingDataDto billingDataDto = mockInstance(new PnPGBillingDataDto());
//        //when
//        PnPGOnboardingData resource = PnPGOnboardingMapper.toOnboardingData(institutionId, productId, model);
//        //then
//        assertNotNull(resource);
//        assertEquals(model.getUsers().size(), resource.getUsers().size());
//        assertEquals(institutionId, resource.getInstitutionExternalId());
//        assertEquals(productId, resource.getProductId());
//        reflectionEqualsByName(billingDataDto, resource.getBillingRequest());
//        reflectionEqualsByName(userDtos.get(0), resource.getUsers().get(0));
//        reflectionEqualsByName(model.getBillingData(), resource.getInstitutionUpdate());
//    }

//    @Test
//    void toOnboardingData_noBusinessName() {
//        //given
//        String institutionId = "institutionId";
//        String productId = "productId";
//        UserDto userDto = mockInstance(new UserDto(), "setProductRole");
//        userDto.setProductRole("");
//        List<UserDto> userDtos = List.of(userDto);
//        PnPGBillingDataDto pnPGBillingDataDto = mockInstance(new PnPGBillingDataDto(), "setTaxCode", "setBusinessName");
//        pnPGBillingDataDto.setTaxCode(institutionId);
//        pnPGBillingDataDto.setBusinessName("");
//        PnPGOnboardingDto model = mockInstance(new PnPGOnboardingDto(), "setUsers", "setBillingData");
//        model.setUsers(userDtos);
//        model.setBillingData(pnPGBillingDataDto);
//        PnPGBillingDataDto billingDataDto = mockInstance(new PnPGBillingDataDto());
//        //when
//        PnPGOnboardingData resource = PnPGOnboardingMapper.toOnboardingData(institutionId, productId, model);
//        //then
//        assertNotNull(resource);
//        assertEquals(model.getUsers().size(), resource.getUsers().size());
//        assertEquals(institutionId, resource.getInstitutionExternalId());
//        assertEquals(productId, resource.getProductId());
//        reflectionEqualsByName(billingDataDto, resource.getBillingRequest());
//        reflectionEqualsByName(userDtos.get(0), resource.getUsers().get(0));
//        reflectionEqualsByName(model.getBillingData(), resource.getInstitutionUpdate());
//    }

    @Test
    void toOnboardingData_null() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        CompanyOnboardingDto onboardingDto = null;
        //when
        PnPGOnboardingData resource = PnPGOnboardingMapper.toOnboardingData(institutionId, productId, onboardingDto);
        //then
        assertNull(resource);
    }

    @Test
    void toResource() {
        //given
        InstitutionLegalAddressData data = mockInstance(new InstitutionLegalAddressData());
        //when
        InstitutionLegalAddressResource resource = PnPGOnboardingMapper.toResource(data);
        //then
        assertNotNull(resource);
        assertEquals(data.getAddress(), resource.getAddress());
        assertEquals(data.getZipCode(), resource.getZipCode());
    }

}