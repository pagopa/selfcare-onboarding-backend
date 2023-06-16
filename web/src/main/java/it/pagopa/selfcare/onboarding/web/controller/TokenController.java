package it.pagopa.selfcare.onboarding.web.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.onboarding.core.TokenService;
import it.pagopa.selfcare.onboarding.web.model.TokenVerifyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/tokens", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "tokens")
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * The verifyToken function is used to verify the token that was created by the onboarding service.
     * It takes in a String tokenId and returns a Token object.
     *
     * @param tokenId tokenId
     * @return The token tokenId
     * * Code: 200, Message: successful operation, DataType: TokenId
     * * Code: 400, Message: Invalid ID supplied, DataType: Problem
     * * Code: 404, Message: Token not found, DataType: Problem
     * * Code: 409, Message: Token already consumed, DataType: Problem
     */
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "${swagger.mscore.token.verify}", notes = "${swagger.mscore.token.verify}")
    @PostMapping("/{tokenId}/verify")
    public ResponseEntity<TokenVerifyResponse> verifyToken(@ApiParam("${swagger.mscore.token.tokenId}")
                                                     @PathVariable("tokenId") String tokenId) {
        log.debug("Verify token identified with {}", tokenId);
        tokenService.verifyToken(tokenId);
        return ResponseEntity.ok().body(TokenVerifyResponse.builder().id(tokenId).build());
    }
}
