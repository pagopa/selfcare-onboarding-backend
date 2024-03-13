package it.pagopa.selfcare.onboarding.core;


import it.pagopa.selfcare.onboarding.connector.api.OnboardingMsConnector;
import it.pagopa.selfcare.onboarding.connector.api.PartyConnector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
public class TokenServiceImplTest {

    @InjectMocks
    private TokenServiceImpl tokenService;

    @Mock
    private PartyConnector partyConnector;

    @Mock
    private OnboardingMsConnector onboardingMsConnector;

    @Test
    void shouldNotVerifyTokenWhenIdIsNull() {
        //given
        String tokenId = null;
        //when
        Executable executable = () -> tokenService.verifyToken(tokenId);
        //then
        Exception e = Assertions.assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("TokenId is required", e.getMessage());
        Mockito.verifyNoInteractions(partyConnector);
    }

    @Test
    void shouldVerifyToken() {
        //given
        String tokenId = "example";
        doNothing().when(partyConnector).tokensVerify(anyString());
        // when
        tokenService.verifyToken(tokenId);
        //then
        Mockito.verify(partyConnector, Mockito.times(1))
                .tokensVerify(tokenId);
    }

    @Test
    void shouldNotCompleteTokenWhenIdIsNull() {
        //given
        String tokenId = null;
        //when
        Executable executable = () -> tokenService.completeToken(tokenId, null);
        //then
        Exception e = Assertions.assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("TokenId is required", e.getMessage());
        Mockito.verifyNoInteractions(partyConnector);
    }

    @Test
    void shouldCompleteToken() throws IOException {
        //given
        String tokenId = "example";
        MockMultipartFile mockMultipartFile = new MockMultipartFile("example", new ByteArrayInputStream("example".getBytes(StandardCharsets.UTF_8)));
        doNothing().when(partyConnector).onboardingTokenComplete(anyString(), any());
        // when
        tokenService.completeToken(tokenId, mockMultipartFile);
        //then
        Mockito.verify(partyConnector, Mockito.times(1))
                .onboardingTokenComplete(tokenId, mockMultipartFile);
    }

    @Test
    void shouldNotCompleteTokenV2WhenIdIsNull() {
        Executable executable = () -> tokenService.completeTokenV2(null, null);
        //then
        Exception e = Assertions.assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("TokenId is required", e.getMessage());
        Mockito.verifyNoInteractions(partyConnector);
    }

    @Test
    void shouldCompleteTokenV2() throws IOException {
        //given
        String tokenId = "example";
        MockMultipartFile mockMultipartFile = new MockMultipartFile("example", new ByteArrayInputStream("example".getBytes(StandardCharsets.UTF_8)));
        doNothing().when(onboardingMsConnector).onboardingTokenComplete(anyString(), any());
        // when
        tokenService.completeTokenV2(tokenId, mockMultipartFile);
        //then
        Mockito.verify(onboardingMsConnector, Mockito.times(1))
                .onboardingTokenComplete(tokenId, mockMultipartFile);
    }

    @Test
    void onboardingPending() {
        //given
        final String onboardingId = "onboardingId";
        // when
        tokenService.verifyOnboarding(onboardingId);
        //then
        Mockito.verify(onboardingMsConnector, Mockito.times(1))
                .onboardingPending(onboardingId);
    }

    @Test
    void onboardingWithUserInfo() {
        //given
        final String onboardingId = "onboardingId";
        // when
        tokenService.getOnboardingWithUserInfo(onboardingId);
        //then
        Mockito.verify(onboardingMsConnector, Mockito.times(1))
                .getOnboardingWithUserInfo(onboardingId);
    }

    @Test
    void shouldDeleteToken() {
        //given
        String tokenId = "example";
        doNothing().when(partyConnector).deleteTokenComplete(anyString());
        // when
        tokenService.deleteToken(tokenId);
        //then
        Mockito.verify(partyConnector, Mockito.times(1))
                .deleteTokenComplete(tokenId);
    }

    @Test
    void approveOnboarding() {
        //given
        String onboardingId = "example";
        doNothing().when(onboardingMsConnector).approveOnboarding(onboardingId);
        // when
        tokenService.approveOnboarding(onboardingId);
        //then
        Mockito.verify(onboardingMsConnector, Mockito.times(1))
                .approveOnboarding(onboardingId);
    }

    @Test
    void rejectOnboarding() {
        //given
        String onboardingId = "example";
        String reason = "reason";
        doNothing().when(onboardingMsConnector).rejectOnboarding(onboardingId, reason);
        // when
        tokenService.rejectOnboarding(onboardingId, reason);
        //then
        Mockito.verify(onboardingMsConnector, Mockito.times(1))
                .rejectOnboarding(onboardingId, reason);
    }
}
