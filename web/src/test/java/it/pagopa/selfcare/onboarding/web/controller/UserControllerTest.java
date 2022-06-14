package it.pagopa.selfcare.onboarding.web.controller;

import it.pagopa.selfcare.onboarding.core.UserService;
import it.pagopa.selfcare.onboarding.core.exception.InvalidUserFieldsException;
import it.pagopa.selfcare.onboarding.web.config.WebTestConfig;
import it.pagopa.selfcare.onboarding.web.handler.OnboardingExceptionHandler;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {UserController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {
        UserController.class,
        WebTestConfig.class,
        OnboardingExceptionHandler.class
})
class UserControllerTest {

    private static final String BASE_URL = "/users";

    @Autowired
    protected MockMvc mvc;

    @MockBean
    protected UserService userServiceMock;


    @Test
    void validate_OK(@Value("classpath:stubs/userDataValidationDto.json") Resource userDataValidationDto) throws Exception {
        //given
        //when
        mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/validate")
                .content(userDataValidationDto.getInputStream().readAllBytes())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));
        //then
        Mockito.verify(userServiceMock, Mockito.times(1))
                .validate(any());
        Mockito.verifyNoMoreInteractions(userServiceMock);
    }


    @Test
    void validate_conflict(@Value("classpath:stubs/userDataValidationDto.json") Resource userDataValidationDto) throws Exception {
        //given
        String productId = "productId";
        Mockito.doThrow(InvalidUserFieldsException.class)
                .when(userServiceMock)
                .validate(any());
        //when
        mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/validate")
                .content(userDataValidationDto.getInputStream().readAllBytes())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(content().string(not(emptyString())));
        //then
        Mockito.verify(userServiceMock, Mockito.times(1))
                .validate(any());
        Mockito.verifyNoMoreInteractions(userServiceMock);
    }

}