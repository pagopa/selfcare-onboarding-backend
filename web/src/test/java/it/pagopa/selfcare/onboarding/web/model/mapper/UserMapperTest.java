package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
import it.pagopa.selfcare.onboarding.web.model.UserDto;
import it.pagopa.selfcare.onboarding.web.model.UserResource;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void toUser() {
        //given
        UserDto model = TestUtils.mockInstance(new UserDto());
        //when
        User resource = UserMapper.toUser(model);
        //then
        assertNotNull(resource);
        TestUtils.reflectionEqualsByName(resource, model);
    }


    @Test
    void toUser_null() {
        //given
        //when
        User resource = UserMapper.toUser(null);
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
        UserResource resource = UserMapper.toResource(model);
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
        UserResource resource = UserMapper.toResource(null);
        //then
        assertNull(resource);
    }

}