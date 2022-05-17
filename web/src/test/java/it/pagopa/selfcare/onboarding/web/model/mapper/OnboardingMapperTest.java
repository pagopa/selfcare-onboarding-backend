package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.Billing;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
import it.pagopa.selfcare.onboarding.connector.model.user.CertifiedField;
import it.pagopa.selfcare.onboarding.connector.model.user.User;
import it.pagopa.selfcare.onboarding.connector.model.user.WorkContact;
import it.pagopa.selfcare.onboarding.web.model.BillingDataDto;
import it.pagopa.selfcare.onboarding.web.model.InstitutionOnboardingInfoResource;
import it.pagopa.selfcare.onboarding.web.model.OnboardingDto;
import it.pagopa.selfcare.onboarding.web.model.UserDto;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        TestUtils.reflectionEqualsByName(billingDataDto, resource.getBilling());
        TestUtils.reflectionEqualsByName(userDtos.get(0), resource.getUsers().get(0));
        TestUtils.reflectionEqualsByName(model.getBillingData(), resource.getInstitutionUpdate());
        assertEquals(model.getOrigin(), resource.getOrigin());
        assertEquals(model.getInstitutionType(), resource.getInstitutionType());
        assertEquals(model.getPricingPlan(), resource.getPricingPlan());
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

    @Test
    void toResource() {
        //given
        InstitutionOnboardingData model = TestUtils.mockInstance(new InstitutionOnboardingData());
        UserInfo manager = TestUtils.mockInstance(new UserInfo());
        String institutionId = UUID.randomUUID().toString();
        User user = TestUtils.mockInstance(new User());
        user.setEmail(TestUtils.mockInstance(new CertifiedField<String>()));
        user.setName(TestUtils.mockInstance(new CertifiedField<String>()));
        user.setFamilyName(TestUtils.mockInstance(new CertifiedField<String>()));
        Map<String, WorkContact> workContactMap = new HashMap<>();
        WorkContact contact = new WorkContact();
        contact.setEmail(TestUtils.mockInstance(new CertifiedField<String>()));
        workContactMap.put(institutionId, contact);
        user.setWorkContacts(workContactMap);
        manager.setUser(user);
        manager.setId(UUID.randomUUID().toString());
        manager.setInstitutionId(institutionId);
        InstitutionInfo institutionInfo = TestUtils.mockInstance(new InstitutionInfo());
        model.setManager(manager);
        model.setInstitution(institutionInfo);
        //when
        InstitutionOnboardingInfoResource resource = OnboardingMapper.toResource(model);
        //then
        TestUtils.reflectionEqualsByName(institutionInfo, resource.getInstitution().getBillingData());
        TestUtils.checkNotNullFields(resource.getManager());
        TestUtils.checkNotNullFields(resource.getInstitution().getBillingData());
        assertEquals(institutionInfo.getInstitutionType(), resource.getInstitution().getInstitutionType());
        assertEquals(institutionInfo.getZipCode(), resource.getInstitution().getBillingData().getZipCode());
    }

    @Test
    void toResource_null() {
        //given
        InstitutionOnboardingData model = null;
        //when
        InstitutionOnboardingInfoResource resource = OnboardingMapper.toResource(model);
        //then
        assertNull(resource);
    }

}