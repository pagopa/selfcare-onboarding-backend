package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.connector.api.PartyConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    private final PartyConnector partyConnector;

    public TokenServiceImpl(PartyConnector partyConnector) {
        this.partyConnector = partyConnector;
    }

    @Override
    public void verifyToken(String tokenId) {
        log.trace("verifyToken start");
        log.debug("verifyToken id = {}", tokenId);
        Assert.notNull(tokenId, "TokenId is required");
        partyConnector.tokensVerify(tokenId);
        log.debug("verifyToken result = success");
        log.trace("verifyToken end");
    }
}
