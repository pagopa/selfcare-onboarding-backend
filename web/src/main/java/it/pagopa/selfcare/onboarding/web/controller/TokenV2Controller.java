package it.pagopa.selfcare.onboarding.web.controller;


import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.web.security.JwtAuthenticationToken;
import it.pagopa.selfcare.onboarding.connector.exceptions.UnauthorizedUserException;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.core.TokenService;
import it.pagopa.selfcare.onboarding.core.UserService;
import it.pagopa.selfcare.onboarding.web.model.OnboardingRequestResource;
import it.pagopa.selfcare.onboarding.web.model.OnboardingVerify;
import it.pagopa.selfcare.onboarding.web.model.ReasonForRejectDto;
import it.pagopa.selfcare.onboarding.web.model.mapper.OnboardingResourceMapper;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.owasp.encoder.Encode;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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
    private final UserService userService;

    private final OnboardingResourceMapper onboardingResourceMapper;

    public TokenV2Controller(TokenService tokenService, UserService userService, OnboardingResourceMapper onboardingResourceMapper) {
        this.tokenService = tokenService;
        this.userService = userService;
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
    @Operation(description = "${swagger.tokens.complete}", summary = "${swagger.tokens.complete}", operationId = "completeUsingPOST")
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
     * The function perform complete operation of an onboarding request receiving onboarding id and contract signed by hte institution.
     * It checks the contract's signature and upload the contract on an azure storage
     * At the end, function triggers async activities related to complete users onboarding
     * that consist of create userInstitution and userInfo records, activate the onboarding for users and sending data to notification queue
     *
     * @param onboardingId String
     * @param contract MultipartFile
     * @return no content
     * * Code: 204, Message: successful operation, DataType: TokenId
     * * Code: 400, Message: Invalid ID supplied, DataType: Problem
     * * Code: 404, Message: Not found, DataType: Problem
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(description = "${swagger.tokens.completeOnboardingUsers}", summary = "${swagger.tokens.completeOnboardingUsers}",
            operationId = "completeOnboardingUsersUsingPOST")
    @PostMapping(value = "/{onboardingId}/complete-onboarding-users")
    public ResponseEntity<Void> completeOnboardingUsers(@ApiParam("${swagger.tokens.onboardingId}")
                                                        @PathVariable(value = "onboardingId") String onboardingId,
                                                        @RequestPart MultipartFile contract) {
        log.trace("complete Onboarding Users start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "complete Onboarding Users tokenId = {}, contract = {}", onboardingId, contract);
        tokenService.completeOnboardingUsers(onboardingId, contract);
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
    @Operation(description = "${swagger.tokens.verify}",
            summary = "${swagger.tokens.verify}", operationId = "verifyOnboardingUsingPOST")
    @PostMapping("/{onboardingId}/verify")
    public OnboardingVerify verifyOnboarding(@ApiParam("${swagger.tokens.onboardingId}") @PathVariable("onboardingId") String onboardingId) {
        String sanitizedOnboardingId = onboardingId.replace("\n", "").replace("\r", "");
        log.debug("Verify token identified with {}", sanitizedOnboardingId);
        final OnboardingData onboardingData = tokenService.verifyOnboarding(sanitizedOnboardingId);
        OnboardingVerify result = onboardingResourceMapper.toOnboardingVerify(onboardingData);
        log.debug("Verify token identified result = {}", result);
        log.trace("Verify token identified end");
        return result;
    }

    @GetMapping(value = "/{onboardingId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "${swagger.tokens.retrieveOnboardingRequest}",
            description = "${swagger.tokens.retrieveOnboardingRequest}", operationId = "retrieveOnboardingRequestUsingGET")
    public OnboardingRequestResource retrieveOnboardingRequest(@ApiParam("${swagger.tokens.onboardingId}")
                                                               @PathVariable("onboardingId")
                                                               String onboardingId) {
        log.trace("retrieveOnboardingRequest start");
        String sanitizedOnboardingId = onboardingId.replace("\n", "").replace("\r", "");
        log.debug("retrieveOnboardingRequest onboardingId = {}", sanitizedOnboardingId);
        final OnboardingData onboardingData = tokenService.getOnboardingWithUserInfo(sanitizedOnboardingId);
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
    @Operation(description = "${swagger.tokens.approveOnboardingRequest}",
            summary = "${swagger.tokens.approveOnboardingRequest}", operationId = "approveOnboardingUsingPOST")
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
    @Operation(summary = "Service to reject a specific onboarding request",
            description = "Service to reject a specific onboarding request", operationId = "rejectOnboardingUsingPOST")
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
    @Operation(summary = "${swagger.tokens.complete}",
            description = "${swagger.tokens.complete}", operationId = "deleteUsingDELETE")
    @DeleteMapping(value = "/{onboardingId}/complete")
    public ResponseEntity<Void> deleteOnboarding(@ApiParam("${swagger.tokens.tokenId}")
                                                 @PathVariable(value = "onboardingId") String onboardingId) {
        log.trace("delete Token start");
        String sanitizedOnboardingId = onboardingId.replace("\n", "").replace("\r", "");
        log.debug("delete Token tokenId = {}", sanitizedOnboardingId);
        tokenService.rejectOnboarding(sanitizedOnboardingId, "REJECTED_BY_USER");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @GetMapping(value = "/{onboardingId}/contract", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "${swagger.tokens.getContract}",
            description = "${swagger.tokens.getContract}", operationId = "getContractUsingGET")
    public ResponseEntity<byte[]> getContract(@ApiParam("${swagger.tokens.onboardingId}")
                                              @PathVariable("onboardingId")
                                              String onboardingId) throws IOException {
        log.trace("getContract start");
        log.debug("getContract onboardingId = {}", onboardingId);
        Resource contract = tokenService.getContract(onboardingId);
        return getResponseEntity(contract);
    }

    @GetMapping(value = "/{onboardingId}/attachment", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "${swagger.tokens.getAttachment}",
            description = "${swagger.tokens.getAttachment}",  operationId = "getAttachmentUsingGET")
    public ResponseEntity<byte[]> getAttachment(@ApiParam("${swagger.tokens.onboardingId}")
                                                @PathVariable("onboardingId")
                                                String onboardingId,
                                                @ApiParam("${swagger.tokens.attachmentName}")
                                                @RequestParam(name = "name") String filename) throws IOException {
        log.trace("getAttachment start");
        String sanitizedFilename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        log.debug("getAttachment onboardingId = {}, filename = {}", Encode.forJava(onboardingId), sanitizedFilename);
        Resource contract = tokenService.getAttachment(onboardingId, filename);
        return getResponseEntity(contract);
    }


    @GetMapping(value = "/{onboardingId}/products/{productId}/aggregates-csv", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "${swagger.tokens.getAggregatesCsv}",
            description = "${swagger.tokens.getAggregatesCsv}", operationId = "getAggregatesCsvUsingGET")
    public ResponseEntity<byte[]> getAggregatesCsv(@ApiParam("${swagger.tokens.onboardingId}") @PathVariable("onboardingId")
                                                   String onboardingIdInput,
                                                   @ApiParam("${swagger.tokens.productId}")
                                                   @PathVariable("productId")
                                                   String productIdInput, Principal principal) throws Exception {

        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) principal;
        SelfCareUser selfCareUser = (SelfCareUser) jwtAuthenticationToken.getPrincipal();
        log.trace("getAggregatesCsv start");
        String onboardingId = Encode.forJava(onboardingIdInput);
        String productId = Encode.forJava(productIdInput);
        log.debug("getAggregatesCsv onboardingId = {}, productId = {}", onboardingId, productId);

        String userUid = selfCareUser.getId();

        if (tokenService.verifyAllowedUserByRole(onboardingId, userUid)
                || userService.isAllowedUserByUid(userUid)) {
            Resource csv = tokenService.getAggregatesCsv(onboardingId, productId);
            return getResponseEntity(csv);
        } else {
            throw new UnauthorizedUserException("Normal-User not allowed to use this endpoint.");
        }

    }


    private HttpHeaders getHttpHeaders(Resource contract) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + contract.getFilename());
        return headers;
    }

    private ResponseEntity<byte[]> getResponseEntity(Resource contract) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = contract.getInputStream();
            byte[] byteArray = IOUtils.toByteArray(inputStream);

            HttpHeaders headers = getHttpHeaders(contract);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(byteArray);
        } finally {
            IOUtils.close(inputStream);
        }
    }
}
