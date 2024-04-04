package it.pagopa.selfcare.onboarding.web.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.core.TokenService;
import it.pagopa.selfcare.onboarding.web.model.OnboardingRequestResource;
import it.pagopa.selfcare.onboarding.web.model.OnboardingVerify;
import it.pagopa.selfcare.onboarding.web.model.ReasonForRejectDto;
import it.pagopa.selfcare.onboarding.web.model.mapper.OnboardingResourceMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

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
    public OnboardingVerify verifyOnboarding(@ApiParam("${swagger.tokens.onboardingId}")
                                                                @PathVariable("onboardingId") String onboardingId) {
        log.debug("Verify token identified with {}", onboardingId);
        final OnboardingData onboardingData = tokenService.verifyOnboarding(onboardingId);
        OnboardingVerify result = onboardingResourceMapper.toOnboardingVerify(onboardingData);
        log.debug("Verify token identified result = {}", result);
        log.trace("Verify token identified end");
        return result;
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

    /**
     * The function is used to approve the onboarding by admin.
     * It takes in a String onboarding id.
     *
     * @param onboardingId onboardingId
     * * Code: 200, Message: successful operation, DataType: TokenId
     * * Code: 400, Message: Invalid ID supplied, DataType: Problem
     * * Code: 404, Message: Token not found, DataType: Problem
     * * Code: 409, Message: Token already consumed, DataType: Problem
     */
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "${swagger.tokens.approveOnboardingRequest}", notes = "${swagger.tokens.approveOnboardingRequest}")
    @PostMapping("/{onboardingId}/approve")
    public void approveOnboarding(@ApiParam("${swagger.tokens.onboardingId}")
                                  @PathVariable("onboardingId") String onboardingId) {
        log.debug("approve onboarding identified with {}", onboardingId);
        tokenService.approveOnboarding(onboardingId);
    }

    /**
     * The function is used to reject the onboarding by admin.
     * It takes in a String onboarding id.
     *
     * @param onboardingId onboardingId
     * * Code: 200, Message: successful operation, DataType: TokenId
     * * Code: 400, Message: Invalid ID supplied, DataType: Problem
     * * Code: 404, Message: Token not found, DataType: Problem
     * * Code: 409, Message: Token already consumed, DataType: Problem
     */
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "${swagger.tokens.rejectOnboardingRequest}")
    @PostMapping("/{onboardingId}/reject")
    public void rejectOnboarding(@ApiParam("${swagger.tokens.onboardingId}")
                                  @PathVariable("onboardingId") String onboardingId,
                                 @RequestBody ReasonForRejectDto reasonForRejectDto) {
        log.debug("reject onboarding identified with {}", onboardingId);
        tokenService.rejectOnboarding(onboardingId, reasonForRejectDto.getReason());
    }

    /**
     * The function delete token on onboarding request
     * it is duplicated with /reject for retro compatibility
     *
     * @param onboardingId String
     * @return no content
     * * Code: 204, Message: successful operation, DataType: TokenId
     * * Code: 400, Message: Invalid ID supplied, DataType: Problem
     * * Code: 404, Message: Not found, DataType: Problem
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "${swagger.tokens.complete}", notes = "${swagger.tokens.complete}")
    @DeleteMapping(value = "/{onboardingId}/complete")
    public ResponseEntity<Void> delete(@ApiParam("${swagger.tokens.tokenId}")
                                       @PathVariable(value = "onboardingId") String onboardingId) {
        log.trace("delete Token start");
        log.debug("delete Token tokenId = {}", onboardingId);
        tokenService.rejectOnboarding(onboardingId, null);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @GetMapping(value = "/{onboardingId}/contract", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.tokens.getContract}")
    public ResponseEntity<byte[]> getContract(@ApiParam("${swagger.tokens.onboardingId}")
                                              @PathVariable("onboardingId")
                                              String onboardingId) throws IOException {
        log.trace("getContract start");
        log.debug("getContract onboardingId = {}", onboardingId);
        Resource contract = tokenService.getContract(onboardingId);
        InputStream inputStream = null;
        try {
            inputStream = contract.getInputStream();
            byte[] byteArray = IOUtils.toByteArray(inputStream);
            log.trace("getContract end");
            return ResponseEntity.ok()
                    .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + contract.getFilename())
                    .body(byteArray);
        } finally {
            IOUtils.close(inputStream);
        }
    }
}
