package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.GeographicTaxonomy;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
import it.pagopa.selfcare.onboarding.web.model.GeographicTaxonomyDto;
import it.pagopa.selfcare.onboarding.web.model.UserDataValidationDto;
import it.pagopa.selfcare.onboarding.web.model.UserDto;
import it.pagopa.selfcare.onboarding.web.model.UserResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.*;

class GeographicTaxonomyMapperTest {

    @Test
    void toGeographicTaxonomy(){
        //given
        GeographicTaxonomyDto model = mockInstance(new GeographicTaxonomyDto());
        //when
        GeographicTaxonomy resource = GeographicTaxonomyMapper.toGeographicTaxonomy(model);
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
        GeographicTaxonomy resource = GeographicTaxonomyMapper.toGeographicTaxonomy(model);
        //then
        assertNull(resource);
    }

}