package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
import it.pagopa.selfcare.onboarding.web.model.UserDataValidationDto;
import it.pagopa.selfcare.onboarding.web.model.UserDto;
import it.pagopa.selfcare.onboarding.web.model.UserResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @ParameterizedTest
    @ValueSource(classes = {
            UserDto.class,
            UserDataValidationDto.class
    })
    void toUser(Class<?> clazz) throws Exception {
        //given
        final Object model = TestUtils.mockInstance(clazz.getDeclaredConstructor().newInstance());
        //when
        User resource;
        if (UserDto.class.isAssignableFrom(clazz)) {
            resource = UserMapper.toUser((UserDto) model);
        } else if (UserDataValidationDto.class.isAssignableFrom(clazz)) {
            resource = UserMapper.toUser((UserDataValidationDto) model);
        } else {
            throw new IllegalArgumentException();
        }
        //then
        assertNotNull(resource);
        TestUtils.reflectionEqualsByName(resource, model);
    }


    @ParameterizedTest
    @ValueSource(classes = {
            UserDto.class,
            UserDataValidationDto.class
    })
    void toUser_null(Class<?> clazz) {
        //given
        //when
        User resource;
        if (UserDto.class.isAssignableFrom(clazz)) {
            resource = UserMapper.toUser((UserDto) null);
        } else if (UserDataValidationDto.class.isAssignableFrom(clazz)) {
            resource = UserMapper.toUser((UserDataValidationDto) null);
        } else {
            throw new IllegalArgumentException();
        }
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