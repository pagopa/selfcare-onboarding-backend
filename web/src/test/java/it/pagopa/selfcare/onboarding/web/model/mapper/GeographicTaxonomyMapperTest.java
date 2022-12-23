package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.GeographicTaxonomy;
import it.pagopa.selfcare.onboarding.web.model.GeographicTaxonomyDto;
import it.pagopa.selfcare.onboarding.web.model.GeographicTaxonomyResource;
import org.junit.jupiter.api.Test;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.*;

class GeographicTaxonomyMapperTest {

    @Test
    void toGeographicTaxonomy(){
        //given
        GeographicTaxonomyDto model = mockInstance(new GeographicTaxonomyDto());
        //when
        GeographicTaxonomy resource = GeographicTaxonomyMapper.fromDto(model);
        //then
        assertNotNull(resource);
        assertEquals(model.getCode(), resource.getCode());
        assertEquals(model.getDesc(), resource.getDesc());
    }

    @Test
    void toGeographicTaxonomy_null(){
        //given
        GeographicTaxonomyDto model = null;
        //when
        GeographicTaxonomy resource = GeographicTaxonomyMapper.fromDto(model);
        //then
        assertNull(resource);
    }

    @Test
    void toGeographicTaxonomyResource(){
        //given
        GeographicTaxonomy model = mockInstance(new GeographicTaxonomy());
        //when
        GeographicTaxonomyResource resource = GeographicTaxonomyMapper.toGeographicTaxonomyResource(model);
        //then
        assertNotNull(resource);
        assertEquals(model.getCode(), resource.getCode());
        assertEquals(model.getDesc(), resource.getDesc());
    }

    @Test
    void toGeographicTaxonomyResource_null(){
        //given
        GeographicTaxonomy model = null;
        //when
        GeographicTaxonomyResource resource = GeographicTaxonomyMapper.toGeographicTaxonomyResource(model);
        //then
        assertNull(resource);
    }

}