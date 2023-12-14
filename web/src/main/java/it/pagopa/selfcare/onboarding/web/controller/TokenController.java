package it.pagopa.selfcare.onboarding.web.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.onboarding.core.TokenService;
import it.pagopa.selfcare.onboarding.web.model.OnboardingMode;
import it.pagopa.selfcare.onboarding.web.model.TokenVerifyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

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
    @ApiOperation(value = "${swagger.tokens.verify}", notes = "${swagger.tokens.verify}")
    @PostMapping("/{tokenId}/verify")
    public ResponseEntity<TokenVerifyResponse> verifyToken(@ApiParam("${swagger.tokens.tokenId}")
                                                     @PathVariable("tokenId") String tokenId) {
        log.debug("Verify token identified with {}", tokenId);
        tokenService.verifyToken(tokenId);
        return ResponseEntity.ok().body(TokenVerifyResponse.builder().id(tokenId).build());
    }

    /**
     * The function complete onboarding request
     *
     * @param tokenId String
     * @param contract MultipartFile
     * @return no content
     * * Code: 204, Message: successful operation, DataType: TokenId
     * * Code: 400, Message: Invalid ID supplied, DataType: Problem
     * * Code: 404, Message: Not found, DataType: Problem
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "${swagger.tokens.complete}", notes = "${swagger.tokens.complete}")
    @PostMapping(value = "/{tokenId}/complete")
    public ResponseEntity<Void> complete(@ApiParam("${swagger.tokens.tokenId}")
                                                   @PathVariable(value = "tokenId") String tokenId,
                                                   @RequestPart MultipartFile contract,
                                                    @RequestParam(value = "mode", required = false) OnboardingMode mode) {
        log.trace("complete Token start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "complete Token tokenId = {}, contract = {}", tokenId, contract);
        if(Objects.isNull(mode) || mode.equals(OnboardingMode.SYNC))
            tokenService.completeToken(tokenId, contract);
        else tokenService.completeTokenAsync(tokenId, contract);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * The function delete token on onboarding request
     *
     * @param tokenId String
     * @return no content
     * * Code: 204, Message: successful operation, DataType: TokenId
     * * Code: 400, Message: Invalid ID supplied, DataType: Problem
     * * Code: 404, Message: Not found, DataType: Problem
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "${swagger.tokens.complete}", notes = "${swagger.tokens.complete}")
    @DeleteMapping(value = "/{tokenId}/complete")
    public ResponseEntity<Void> delete(@ApiParam("${swagger.tokens.tokenId}")
                                         @PathVariable(value = "tokenId") String tokenId) {
        log.trace("delete Token start");
        log.debug("delete Token tokenId = {}", tokenId);
        tokenService.deleteToken(tokenId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
