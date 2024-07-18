package it.pagopa.selfcare.onboarding.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.onboarding.common.InstitutionType;

import it.pagopa.selfcare.onboarding.core.InstitutionService;
import it.pagopa.selfcare.onboarding.web.model.CompanyOnboardingDto;
import it.pagopa.selfcare.onboarding.web.model.InstitutionResource;
import it.pagopa.selfcare.onboarding.web.model.OnboardingProductDto;
import it.pagopa.selfcare.onboarding.web.model.VerifyAggregatesResponse;
import it.pagopa.selfcare.onboarding.web.model.mapper.InstitutionMapper;
import it.pagopa.selfcare.onboarding.web.model.mapper.OnboardingResourceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;

@Slf4j
@RestController
@RequestMapping(value = "/v2/institutions", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "institutions")
public class InstitutionV2Controller {

    private final InstitutionService institutionService;
    private final OnboardingResourceMapper onboardingResourceMapper;

    private static final String ONBOARDING_START = "onboarding start";
    private static final String ONBOARDING_END = "onboarding end";

    @Autowired
    public InstitutionV2Controller(InstitutionService institutionService, OnboardingResourceMapper onboardingResourceMapper) {
        this.institutionService = institutionService;
        this.onboardingResourceMapper = onboardingResourceMapper;
    }

    @ApiResponse(responseCode = "403",
            description = "Forbidden",
            content = {
                    @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            })
    @PostMapping(value = "/onboarding")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.onboarding.subunit}")
    public void onboarding(@RequestBody @Valid OnboardingProductDto request) {
        log.trace(ONBOARDING_START);
        log.debug("onboarding request = {}", request);
        if (Boolean.TRUE.equals(request.getIsAggregator())) {
            institutionService.onboardingPaAggregator(onboardingResourceMapper.toEntity(request));
        } else {
            institutionService.onboardingProductV2(onboardingResourceMapper.toEntity(request));
        }
        log.trace(ONBOARDING_END);
    }

    @ApiResponse(responseCode = "403",
            description = "Forbidden",
            content = {
                    @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            })
    @PostMapping(value = "/company/onboarding")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.onboarding.subunit}")
    public void onboarding(@RequestBody @Valid CompanyOnboardingDto request) {
        log.trace(ONBOARDING_START);
        log.debug("onboarding request = {}", request);
        institutionService.onboardingCompanyV2(onboardingResourceMapper.toEntity(request));
        log.trace(ONBOARDING_END);
    }

    @ApiResponse(responseCode = "403",
            description = "Forbidden",
            content = {
                    @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.onboarding.subunit}", nickname = "v2GetInstitutionByFilters")
    public List<InstitutionResource> getInstitution(@ApiParam("${swagger.onboarding.institutions.model.productFilter}")
                                                    @RequestParam(value = "productId")
                                                    String productId,
                                                    @ApiParam("${swagger.onboarding.institutions.model.taxCode}")
                                                    @RequestParam(value = "taxCode", required = false)
                                                    String taxCode,
                                                    @ApiParam("${swagger.onboarding.institutions.model.origin}")
                                                    @RequestParam(value = "origin", required = false)
                                                    String origin,
                                                    @ApiParam("${swagger.onboarding.institutions.model.originId}")
                                                    @RequestParam(value = "originId", required = false)
                                                    String originId,
                                                    @ApiParam("${swagger.onboarding.institutions.model.subunitCode}")
                                                    @RequestParam(value = "subunitCode", required = false)
                                                    String subunitCode) {
        log.trace("getInstitution start");
        final List<InstitutionResource> institutions = institutionService.getByFilters(productId, taxCode, origin, originId, subunitCode)
                .stream()
                .map(InstitutionMapper::toResource)
                .toList();
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitution result = {}", institutions);
        log.trace("getInstitution end");
        return institutions;
    }

    @PostMapping(value = "/onboarding/aggregation/verification")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.onboarding.subunit}")
    public VerifyAggregatesResponse verifyAggregatesCsv(@RequestParam("aggregates") MultipartFile file,
                                                        @RequestParam("institutionType") InstitutionType institutionType) {
        log.trace("Verify Aggregates Csv start");
        log.debug("Verify Aggregates Csv start for institutionType {}", institutionType);
        VerifyAggregatesResponse response = onboardingResourceMapper.toVerifyAggregatesResponse(institutionService.validateAggregatesCsv(file));
        log.trace("Verify Aggregates Csv end");
        return response;
    }

    @RequestMapping(method = HEAD, value = "/onboarding/recipientCode/verification")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.onboarding.checkRecipientCode}")
    public boolean checkRecipientCode(@RequestParam(value = "originId") String originId,
                                      @RequestParam(value = "subunitCode") String subunitCode) {
        log.trace("Check recipientCode start");
        log.debug("Check recipientCode start for institution with originId {} and subunitCode {}", originId, subunitCode);
        boolean response = institutionService.checkRecipientCode(originId, subunitCode);
        log.trace("Check recipientCode start");
        return response;
    }

}
