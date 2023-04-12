package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.onboarding.web.model.UserDataValidationDto;
import it.pagopa.selfcare.onboarding.web.model.UserDto;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertNull;

class PnPGUserMapperTest {
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
}