package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.AssistanceContacts;
import it.pagopa.selfcare.onboarding.connector.model.institutions.CompanyInformations;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.Billing;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.GeographicTaxonomy;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.user.CertifiedField;
import it.pagopa.selfcare.onboarding.connector.model.user.WorkContact;
import it.pagopa.selfcare.onboarding.web.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.commons.utils.TestUtils.reflectionEqualsByName;
import static org.junit.jupiter.api.Assertions.*;

class OnboardingMapperTest {

    @Test
    void toOnboardingData() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        List<UserDto> userDtos = List.of(mockInstance(new UserDto()));
        OnboardingDto model = mockInstance(new OnboardingDto());
        BillingDataDto billingDataDto = mockInstance(new BillingDataDto());
        PspDataDto pspDataDto = mockInstance(new PspDataDto());
        List<GeographicTaxonomyDto> geographicTaxonomyDtos = List.of(mockInstance(new GeographicTaxonomyDto()));
        model.setBillingData(billingDataDto);
        model.setUsers(userDtos);
        model.setPspData(pspDataDto);
        model.setGeographicTaxonomies(geographicTaxonomyDtos);
        //when
        OnboardingData resource = OnboardingMapper.toOnboardingData(institutionId, productId, model);
        //then
        assertNotNull(resource);
        assertEquals(model.getUsers().size(), resource.getUsers().size());
        assertEquals(model.getGeographicTaxonomies().size(), resource.getInstitutionUpdate().getGeographicTaxonomies().size());
        assertEquals(institutionId, resource.getInstitutionExternalId());
        assertEquals(productId, resource.getProductId());
        reflectionEqualsByName(billingDataDto, resource.getBilling());
        reflectionEqualsByName(userDtos.get(0), resource.getUsers().get(0));
        reflectionEqualsByName(model.getBillingData(), resource.getInstitutionUpdate());
        reflectionEqualsByName(model.getPspData(), resource.getInstitutionUpdate().getPaymentServiceProvider(), "dpoData");
        reflectionEqualsByName(model.getPspData().getDpoData(), resource.getInstitutionUpdate().getDataProtectionOfficer());
        reflectionEqualsByName(geographicTaxonomyDtos.get(0), resource.getInstitutionUpdate().getGeographicTaxonomies().get(0));
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
        BillingDataDto model = mockInstance(new BillingDataDto());
        //when
        Billing resource = OnboardingMapper.fromDto(model);
        //then
        assertNotNull(resource);
        reflectionEqualsByName(model, resource);
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
        InstitutionOnboardingData model = mockInstance(new InstitutionOnboardingData());

        WorkContact contact = new WorkContact();
        contact.setEmail(mockInstance(new CertifiedField<String>()));

        InstitutionInfo institutionInfo = mockInstance(new InstitutionInfo());
        model.setInstitution(institutionInfo);
        model.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        //when
        InstitutionOnboardingInfoResource resource = OnboardingMapper.toResource(model);
        //then
        reflectionEqualsByName(institutionInfo, resource.getInstitution().getBillingData());
        reflectionEqualsByName(model.getAssistanceContacts(), resource.getInstitution().getAssistanceContacts());
        reflectionEqualsByName(model.getCompanyInformations(), resource.getInstitution().getCompanyInformations());

        TestUtils.checkNotNullFields(resource.getInstitution().getBillingData());
        assertEquals(institutionInfo.getInstitutionType(), resource.getInstitution().getInstitutionType());
        assertEquals(institutionInfo.getZipCode(), resource.getInstitution().getBillingData().getZipCode());
        assertEquals(model.getGeographicTaxonomies().get(0).getCode(), resource.getGeographicTaxonomies().get(0).getCode());
        assertEquals(model.getGeographicTaxonomies().get(0).getDesc(), resource.getGeographicTaxonomies().get(0).getDesc());
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

    @Test
    void toResourceAssistanceContacts() {
        // given
        AssistanceContacts model = mockInstance(new AssistanceContacts());
        model.setSupportEmail("example@example.com");
        // when
        AssistanceContactsResource resource = OnboardingMapper.toResource(model);
        // then
        assertNotNull(resource);
        reflectionEqualsByName(model, resource);
    }

    @Test
    void toResourceAssistanceContacts_null() {
        // given
        AssistanceContacts model = null;
        // when
        AssistanceContactsResource resource = OnboardingMapper.toResource(model);
        // then
        assertNull(resource);
    }

    @Test
    void toResourceCompanyInformations() {
        // given
        CompanyInformations model = mockInstance(new CompanyInformations());
        // when
        CompanyInformationsResource resource = OnboardingMapper.toResource(model);
        // then
        assertNotNull(resource);
        reflectionEqualsByName(model, resource);
    }

    @Test
    void toResourceCompanyInformations_null() {
        // given
        CompanyInformations model = null;
        // when
        CompanyInformationsResource resource = OnboardingMapper.toResource(model);
        // then
        assertNull(resource);
    }
}