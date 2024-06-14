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
    void shouldNotCompleteOnboardingUsersWhenIdIsNull() {
        Executable executable = () -> tokenService.completeOnboardingUsers(null, null);
        //then
        Exception e = Assertions.assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("TokenId is required", e.getMessage());
        Mockito.verifyNoInteractions(partyConnector);
    }

    @Test
    void shouldCompleteOnboardingUsers() throws IOException {
        //given
        String tokenId = "example";
        MockMultipartFile mockMultipartFile = new MockMultipartFile("example", new ByteArrayInputStream("example".getBytes(StandardCharsets.UTF_8)));
        doNothing().when(onboardingMsConnector).onboardingUsersComplete(anyString(), any());
        // when
        tokenService.completeOnboardingUsers(tokenId, mockMultipartFile);
        //then
        Mockito.verify(onboardingMsConnector, Mockito.times(1))
                .onboardingUsersComplete(tokenId, mockMultipartFile);
    }

    @Test
    void verifyOnboarding() {
        //given
        final String onboardingId = "onboardingId";
        // when
        tokenService.verifyOnboarding(onboardingId);
        //then
        Mockito.verify(onboardingMsConnector, Mockito.times(1))
                .getOnboarding(onboardingId);
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
        final String onboardingId = "example";
        final String reason = "reason";
        doNothing().when(onboardingMsConnector).rejectOnboarding(onboardingId, reason);
        // when
        tokenService.rejectOnboarding(onboardingId, reason);
        //then
        Mockito.verify(onboardingMsConnector, Mockito.times(1))
                .rejectOnboarding(onboardingId, reason);
    }

    @Test
    void getContract() {
        //given
        final String onboardingId = "onboardingId";
        // when
        tokenService.getContract(onboardingId);
        //then
        Mockito.verify(onboardingMsConnector, Mockito.times(1))
                .getContract(onboardingId);
    }
}
