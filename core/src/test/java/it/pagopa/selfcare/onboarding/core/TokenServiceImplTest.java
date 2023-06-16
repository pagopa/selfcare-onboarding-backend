package it.pagopa.selfcare.onboarding.core;


import it.pagopa.selfcare.onboarding.connector.api.PartyConnector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
public class TokenServiceImplTest {

    @InjectMocks
    private TokenServiceImpl tokenService;

    @Mock
    private PartyConnector partyConnector;

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
}
