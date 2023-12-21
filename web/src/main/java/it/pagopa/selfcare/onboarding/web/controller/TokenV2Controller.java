package it.pagopa.selfcare.onboarding.web.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.core.TokenService;
import it.pagopa.selfcare.onboarding.web.model.OnboardingRequestResource;
import it.pagopa.selfcare.onboarding.web.model.TokenVerifyResponse;
import it.pagopa.selfcare.onboarding.web.model.mapper.OnboardingResourceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping(value = "/v2/tokens", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "tokens")
public class TokenV2Controller {

    private final TokenService tokenService;

    private final OnboardingResourceMapper onboardingResourceMapper;

    public TokenV2Controller(TokenService tokenService, OnboardingResourceMapper onboardingResourceMapper) {
        this.tokenService = tokenService;
        this.onboardingResourceMapper = onboardingResourceMapper;
    }

    /**
     * The function perform complete operation of an onboarding request receiving onboarding id and contract signed by hte institution.
     * It checks the contract's signature and upload the contract on an azure storage
     * At the end, function triggers async activities related to complete onboarding
     * that consist of create the institution, activate the onboarding and sending data to notification queue
     *
     * @param onboardingId String
     * @param contract MultipartFile
     * @return no content
     * * Code: 204, Message: successful operation, DataType: TokenId
     * * Code: 400, Message: Invalid ID supplied, DataType: Problem
     * * Code: 404, Message: Not found, DataType: Problem
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "${swagger.tokens.complete}", notes = "${swagger.tokens.complete}")
    @PostMapping(value = "/{onboardingId}/complete")
    public ResponseEntity<Void> complete(@ApiParam("${swagger.tokens.onboardingId}")
                                                   @PathVariable(value = "onboardingId") String onboardingId,
                                                   @RequestPart MultipartFile contract) {
        log.trace("complete Token start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "complete Token tokenId = {}, contract = {}", onboardingId, contract);
        tokenService.completeTokenV2(onboardingId, contract);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * The verifyOnboarding function is used to verify the onboarding has already consumed.
     * It takes in a String onboarding and returns a Token object.
     *
     * @param onboardingId onboardingId
     * @return The onboardingId
     * * Code: 200, Message: successful operation, DataType: TokenId
     * * Code: 400, Message: Invalid ID supplied, DataType: Problem
     * * Code: 404, Message: Token not found, DataType: Problem
     * * Code: 409, Message: Token already consumed, DataType: Problem
     */
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "${swagger.tokens.verify}", notes = "${swagger.tokens.verify}")
    @PostMapping("/{onboardingId}/verify")
    public ResponseEntity<TokenVerifyResponse> verifyOnboarding(@ApiParam("${swagger.tokens.onboardingId}")
                                                           @PathVariable("onboardingId") String onboardingId) {
        log.debug("Verify token identified with {}", onboardingId);
        tokenService.verifyOnboarding(onboardingId);
        return ResponseEntity.ok().body(TokenVerifyResponse.builder().id(onboardingId).build());
    }


    @GetMapping(value = "/{onboardingId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.tokens.retrieveOnboardingRequest}")
    public OnboardingRequestResource retrieveOnboardingRequest(@ApiParam("${swagger.tokens.onboardingId}")
                                                               @PathVariable("onboardingId")
                                                               String onboardingId) {
        log.trace("retrieveOnboardingRequest start");
        log.debug("retrieveOnboardingRequest onboardingId = {}", onboardingId);
        final OnboardingData onboardingData = tokenService.getOnboardingWithUserInfo(onboardingId);
        OnboardingRequestResource result = onboardingResourceMapper.toOnboardingRequestResource(onboardingData);
        log.debug("retrieveOnboardingRequest result = {}", result);
        log.trace("retrieveOnboardingRequest end");
        return result;
    }

}
