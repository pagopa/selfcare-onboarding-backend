package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.Billing;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.GeographicTaxonomy;
import it.pagopa.selfcare.onboarding.web.model.InstitutionResource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;

class InstitutionMapperTest {


    @Test
    void toResource_institutionInfo() {
        //given
        InstitutionInfo model = TestUtils.mockInstance(new InstitutionInfo(), "setId");
        model.setId(randomUUID().toString());
        model.setBilling(TestUtils.mockInstance(new Billing()));
        //when
        InstitutionResource resource = InstitutionMapper.toResource(model);
        //then
        assertNotNull(resource);
        assertEquals(resource.getId().toString(), model.getId());
        assertEquals(resource.getExternalId(), model.getExternalId());
        assertEquals(resource.getDescription(), model.getDescription());
        assertEquals(resource.getAddress(), model.getAddress());
        assertEquals(resource.getDigitalAddress(), model.getDigitalAddress());
        assertEquals(resource.getZipCode(), model.getZipCode());
        assertEquals(resource.getTaxCode(), model.getTaxCode());
        assertEquals(resource.getOrigin(), model.getOrigin());    }

    @Test
    void toResource_nullInstitutionInfo() {
        //given
        final InstitutionInfo model = null;
        //when
        InstitutionResource resource = InstitutionMapper.toResource(model);
        //then
        assertNull(resource);
    }

}