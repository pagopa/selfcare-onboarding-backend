package it.pagopa.selfcare.onboarding.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionPnPGInfo;
import it.pagopa.selfcare.onboarding.core.PnPGInstitutionService;
import it.pagopa.selfcare.onboarding.web.model.InstitutionPnPGResource;
import it.pagopa.selfcare.onboarding.web.model.PnPGOnboardingDto;
import it.pagopa.selfcare.onboarding.web.model.UserDto;
import it.pagopa.selfcare.onboarding.web.model.mapper.InstitutionPnPGMapper;
import it.pagopa.selfcare.onboarding.web.model.mapper.PnPGOnboardingMapper;
import it.pagopa.selfcare.onboarding.web.model.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/pnPGInstitutions", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "pnPGInstitutions")
public class PnPGInstitutionController {

    private final PnPGInstitutionService pnPGInstitutionService;


    @Autowired
    public PnPGInstitutionController(PnPGInstitutionService pnPGInstitutionService) {
        this.pnPGInstitutionService = pnPGInstitutionService;
    }

    @PostMapping(value = "")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.onboarding.pnPGInstitutions.api.getInstitutionsByUser}")
    public InstitutionPnPGResource getInstitutionsByUser(@RequestBody
                                                         @Valid
                                                         UserDto userDto) {
        log.trace("getInstitutionsByUserId start");
        log.debug("getInstitutionsByUserId userDto = {}", userDto);
        InstitutionPnPGInfo institutionPnPGInfo = pnPGInstitutionService.getInstitutionsByUser(UserMapper.toUser(userDto));
        InstitutionPnPGResource institutionPnPGResources = InstitutionPnPGMapper.toResource(institutionPnPGInfo);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionsByUserId result = {}", institutionPnPGResources);
        log.trace("getInstitutionsByUserId end");
        return institutionPnPGResources;
    }

    @ApiResponse(responseCode = "403",
            description = "Forbidden",
            content = {
                    @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            })
    @PostMapping(value = "/{externalInstitutionId}/products/{productId}/onboarding")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "", notes = "${swagger.onboarding.pnPGInstitutions.api.onboarding}")
    public void onboardingPG(@ApiParam("${swagger.onboarding.pnPGInstitutions.model.externalId}")
                             @PathVariable("externalInstitutionId")
                             String externalInstitutionId,
                             @ApiParam("${swagger.onboarding.product.model.id}")
                             @PathVariable("productId")
                             String productId,
                             @RequestBody
                             @Valid
                             PnPGOnboardingDto request) {
        log.trace("onboarding PNPG start");
        log.debug("onboarding PNPG institutionId = {}, productId = {}, request = {}", externalInstitutionId, productId, request);
        pnPGInstitutionService.onboarding(PnPGOnboardingMapper.toOnboardingData(externalInstitutionId, productId, request));
        log.trace("onboarding PNPG end");
    }


}