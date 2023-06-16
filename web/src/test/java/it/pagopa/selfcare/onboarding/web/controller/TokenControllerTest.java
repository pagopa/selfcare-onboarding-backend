package it.pagopa.selfcare.onboarding.web.controller;


import it.pagopa.selfcare.onboarding.core.TokenService;
import it.pagopa.selfcare.onboarding.web.config.WebTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {TokenController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {TokenController.class, WebTestConfig.class})
public class TokenControllerTest {

    @Autowired
    protected MockMvc mvc;

    @MockBean
    private TokenService tokenService;

    @Test
    void shouldVerifyToken() throws Exception {
        //given
        String id = UUID.randomUUID().toString();
        doNothing().when(tokenService).verifyToken(any());

        //when
        mvc.perform(MockMvcRequestBuilders
                        .post("/tokens/{tokenId}/verify", id)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)));

        //then
       verify(tokenService, times(1)).verifyToken(id);
    }
}
