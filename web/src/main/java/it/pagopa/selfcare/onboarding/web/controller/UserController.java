package it.pagopa.selfcare.onboarding.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.commons.web.security.JwtAuthenticationToken;
import it.pagopa.selfcare.onboarding.core.UserService;
import it.pagopa.selfcare.onboarding.web.model.CheckManagerResponse;
import it.pagopa.selfcare.onboarding.web.model.ManagerInfoResponse;
import it.pagopa.selfcare.onboarding.web.model.OnboardingUserDto;
import it.pagopa.selfcare.onboarding.web.model.UserDataValidationDto;
import it.pagopa.selfcare.onboarding.web.model.mapper.OnboardingResourceMapper;
import it.pagopa.selfcare.onboarding.web.model.mapper.UserMapper;
import it.pagopa.selfcare.onboarding.web.model.mapper.UserResourceMapper;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.security.Principal;

import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;


@Slf4j
@RestController
@RequestMapping(value = "/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "user")
public class UserController {

    private final UserService userService;
    private final OnboardingResourceMapper onboardingResourceMapper;
    private final UserResourceMapper userResourceMapper;

    @Autowired
    public UserController(UserService userService,
                          OnboardingResourceMapper onboardingResourceMapper,
                          UserResourceMapper userResourceMapper) {
        this.userService = userService;
        this.onboardingResourceMapper = onboardingResourceMapper;
        this.userResourceMapper = userResourceMapper;
    }

    @PostMapping("/validate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponse(responseCode = "409",
            description = "Conflict",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            })
    @ApiOperation(value = "", notes = "${swagger.onboarding.user.api.validate}")
    public void validate(@RequestBody @Valid UserDataValidationDto request) {
        log.trace("validate start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "validate request = {}", request);
        userService.validate(UserMapper.toUser(request));
        log.trace("validate end");
    }

    @ApiResponse(responseCode = "403",
            description = "Forbidden",
            content = {
                    @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            })
    @PostMapping(value = "/onboarding")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "", notes = "${swagger.onboarding.users.api.onboarding}")
    public void onboarding(@RequestBody @Valid OnboardingUserDto request) {
        log.trace("onboarding start");
        log.debug("onboarding request = {}", request);
        userService.onboardingUsers(onboardingResourceMapper.toEntity(request));
        log.trace("onboarding end");
    }


    @ApiResponse(responseCode = "403",
            description = "Forbidden",
            content = {
                    @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            })
    @PostMapping(value = "/onboarding/aggregator")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "", notes = "${swagger.onboarding.users.api.onboarding-aggregator}")
    public void onboardingAggregator(@RequestBody @Valid OnboardingUserDto request) {
        log.trace("onboardingAggregator start");
        log.debug("onboardingAggregator request = {}", Encode.forJava(request.toString()));
        userService.onboardingUsersAggregator(onboardingResourceMapper.toEntity(request));
        log.trace("onboardingAggregator end");
    }

    @ApiResponse(responseCode = "403",
            description = "Forbidden",
            content = {
                    @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            })
    @PostMapping(value = "/check-manager")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.onboarding.users.api.check-manager}", nickname = "checkManager")
    public CheckManagerResponse checkManager(@RequestBody @Valid OnboardingUserDto request) {
        log.trace("onboarding start");
        boolean checkManager =  userService.checkManager(onboardingResourceMapper.toEntity(request));
        log.trace("onboarding end");
        return new CheckManagerResponse(checkManager);
    }

    @ApiResponse(responseCode = "403",
            description = "Forbidden",
            content = {
                    @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            })
    @GetMapping(value = "/onboarding/{onboardingId}/manager")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.onboarding.users.api.check-manager}", nickname = "checkManager")
    public ManagerInfoResponse getManagerInfo(@PathVariable("onboardingId") String onboardingId, Principal principal) {
        log.trace("getManagerInfo start");
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) principal;
        SelfCareUser selfCareUser = (SelfCareUser) jwtAuthenticationToken.getPrincipal();

        ManagerInfoResponse managerInfoResponse = userResourceMapper.toManagerInfoResponse(userService.getManagerInfo(onboardingId, selfCareUser.getFiscalCode()));
        log.trace("getManagerInfo end");
        return managerInfoResponse;
    }

}
