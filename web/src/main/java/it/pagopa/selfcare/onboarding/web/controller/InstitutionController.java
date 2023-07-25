package it.pagopa.selfcare.onboarding.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.commons.web.security.JwtAuthenticationToken;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionLegalAddressData;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.MatchInfoResult;
import it.pagopa.selfcare.onboarding.connector.model.institutions.infocamere.InstitutionInfoIC;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionType;
import it.pagopa.selfcare.onboarding.core.InstitutionService;
import it.pagopa.selfcare.onboarding.web.model.*;
import it.pagopa.selfcare.onboarding.web.model.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;

@Slf4j
@RestController
@RequestMapping(value = "/institutions", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "institutions")
public class InstitutionController {

    private final InstitutionService institutionService;
    private final OnboardingResourceMapper onboardingResourceMapper;


    @Autowired
    public InstitutionController(InstitutionService institutionService, OnboardingResourceMapper onboardingResourceMapper) {
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
        log.trace("onboarding start");
        log.debug("onboarding request = {}", request);
        if (InstitutionType.PSP.equals(request.getInstitutionType()) && request.getPspData() == null) {
            throw new ValidationException("Field 'pspData' is required for PSP institution onboarding");
        }
        institutionService.onboardingProduct(onboardingResourceMapper.toEntity(request));
        log.trace("onboarding end");
    }

    @Deprecated
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403",
                    description = "Forbidden",
                    content = {
                            @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                    schema = @Schema(implementation = Problem.class))
                    }),
            @ApiResponse(responseCode = "409",
                    description = "Conflict",
                    content = {
                            @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                    schema = @Schema(implementation = Problem.class))
                    }),
    })
    @PostMapping(value = "/{externalInstitutionId}/products/{productId}/onboarding")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.onboarding}")
    public void onboarding(@ApiParam("${swagger.onboarding.institutions.model.externalId}")
                           @PathVariable("externalInstitutionId")
                                   String externalInstitutionId,
                           @ApiParam("${swagger.onboarding.product.model.id}")
                           @PathVariable("productId")
                                   String productId,
                           @RequestBody
                           @Valid
                                   OnboardingDto request) {
        log.trace("onboarding start");
        log.debug("onboarding institutionId = {}, productId = {}, request = {}", externalInstitutionId, productId, request);
        if (InstitutionType.PSP.equals(request.getInstitutionType()) && request.getPspData() == null) {
            throw new ValidationException("Field 'pspData' is required for PSP institution onboarding");
        }
        institutionService.onboarding(OnboardingMapper.toOnboardingData(externalInstitutionId, productId, request));
        log.trace("onboarding end");
    }

    @Deprecated
    @GetMapping(value = "/{externalInstitutionId}/products/{productId}/onboarded-institution-info")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.getInstitutionOnboardingInfo}")
    public InstitutionOnboardingInfoResource getInstitutionOnboardingInfo(@ApiParam("${swagger.onboarding.institutions.model.externalId}")
                                                                          @PathVariable("externalInstitutionId")
                                                                                  String externalInstitutionId,
                                                                          @ApiParam("${swagger.onboarding.product.model.id}")
                                                                          @PathVariable("productId")
                                                                          String productId) {
        log.trace("getInstitutionOnBoardingInfo start");
        log.debug("getInstitutionOnBoardingInfo institutionId = {}, productId = {}", externalInstitutionId, productId);
        InstitutionOnboardingData institutionOnboardingData = institutionService.getInstitutionOnboardingData(externalInstitutionId, productId);
        InstitutionOnboardingInfoResource result = OnboardingMapper.toResource(institutionOnboardingData);
        log.debug("getInstitutionOnBoardingInfo result = {}", result);
        log.trace("getInstitutionOnBoardingInfo end");
        return result;
    }

    @GetMapping(value = "/onboarding/")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.getInstitutionOnboardingInfo}")
    public InstitutionOnboardingInfoResource getInstitutionOnboardingInfo(@ApiParam("${swagger.onboarding.institutions.model.taxCode}")
                                                                              @RequestParam("taxCode")
                                                                              String taxCode,
                                                                          @ApiParam("${swagger.onboarding.institutions.model.subunitCode}")
                                                                              @RequestParam(value = "subunitCode", required = false)
                                                                              String subunitCode,
                                                                          @ApiParam("${swagger.onboarding.product.model.id}")
                                                                              @RequestParam("productId")
                                                                              String productId) {
        log.trace("getInstitutionOnBoardingInfo start");
        log.debug("getInstitutionOnBoardingInfo taxCode = {}, subunitCode = {}, productId = {}", taxCode, subunitCode, productId);
        InstitutionOnboardingData institutionOnboardingData = institutionService.getInstitutionOnboardingData(taxCode, subunitCode, productId);
        InstitutionOnboardingInfoResource result = OnboardingMapper.toResource(institutionOnboardingData);
        log.debug("getInstitutionOnBoardingInfo result = {}", result);
        log.trace("getInstitutionOnBoardingInfo end");
        return result;
    }

    @GetMapping(value = "/{externalInstitutionId}/geographicTaxonomy")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.getInstitutionGeographicTaxonomy}")
    public List<GeographicTaxonomyResource> getInstitutionGeographicTaxonomy(@ApiParam("${swagger.onboarding.institutions.model.externalId}")
                                                                             @PathVariable("externalInstitutionId")
                                                                             String externalInstitutionId) {
        log.trace("getInstitutionGeographicTaxonomy start");
        log.debug("getInstitutionGeographicTaxonomy institutionId = {}", externalInstitutionId);
        List<GeographicTaxonomyResource> geographicTaxonomies = institutionService.getGeographicTaxonomyList(externalInstitutionId)
                .stream()
                .map(GeographicTaxonomyMapper::toResource)
                .collect(Collectors.toList());
        log.debug("getInstitutionGeographicTaxonomy result = {}", geographicTaxonomies);
        log.trace("getInstitutionGeographicTaxonomy end");
        return geographicTaxonomies;
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.getInstitutions}")
    public List<InstitutionResource> getInstitutions(@ApiParam("${swagger.onboarding.institutions.model.productFilter}")
                                                     @RequestParam(value = "productFilter", required = false)
                                                     String productFilter) {
        log.trace("getInstitutions start");
        List<InstitutionResource> institutionResources = institutionService.getInstitutions(productFilter)
                .stream()
                .map(InstitutionMapper::toResource)
                .collect(Collectors.toList());
        log.debug("getInstitutions result = {}", institutionResources);
        log.trace("getInstitutions end");
        return institutionResources;
    }


    @ApiResponse(responseCode = "403",
            description = "Forbidden",
            content = {
                    @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            })
    @RequestMapping(method = HEAD, value = "/{externalInstitutionId}/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.verifyOnboarding}")
    public void verifyOnboarding(@ApiParam("${swagger.onboarding.institutions.model.externalId}")
                                 @PathVariable("externalInstitutionId")
                                         String externalInstitutionId,
                                 @ApiParam("${swagger.onboarding.product.model.id}")
                                 @PathVariable("productId")
                                         String productId) {
        log.trace("verifyOnboarding start");
        log.debug("verifyOnboarding externalInstitutionId = {}, productId = {}", externalInstitutionId, productId);
        institutionService.verifyOnboarding(externalInstitutionId, productId);
        log.trace("verifyOnboarding end");
    }

    @ApiResponse(responseCode = "403",
            description = "Forbidden",
            content = {
                    @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            })
    @RequestMapping(method = HEAD, value = "/onboarding")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.verifyOnboarding}")
    public void verifyOnboarding(@ApiParam("${swagger.onboarding.institutions.model.taxCode}")
                                 @RequestParam("taxCode")
                                 String taxCode,
                                 @ApiParam("${swagger.onboarding.institutions.model.subunitCode}")
                                 @RequestParam(value = "subunitCode", required = false)
                                 String subunitCode,
                                 @ApiParam("${swagger.onboarding.product.model.id}")
                                 @RequestParam("productId")
                                 String productId) {
        log.trace("verifyOnboarding start");
        log.debug("verifyOnboarding taxCode = {}, subunitCode = {}, productId = {}", taxCode, subunitCode, productId);
        institutionService.verifyOnboarding(taxCode, subunitCode, productId);
        log.trace("verifyOnboarding end");
    }

    @Deprecated
    @PostMapping(value = "")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.getInstitutionsByUser}")
    public InstitutionResourceIC getInstitutionsByUser(@RequestBody
                                                           @Valid
                                                           UserDto userDto, Principal principal) {
        log.trace("getInstitutionsByUser start");

        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) principal;
        SelfCareUser selfCareUser = (SelfCareUser) jwtAuthenticationToken.getPrincipal();

        InstitutionInfoIC institutionInfoIC = institutionService.getInstitutionsByUser(selfCareUser.getFiscalCode());
        InstitutionResourceIC institutionResourceIC = InstitutionMapper.toResource(institutionInfoIC);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionsByUser result = {}", institutionResourceIC);
        log.trace("getInstitutionsByUser end");
        return institutionResourceIC;
    }

    @GetMapping(value = "/from-infocamere/")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.getInstitutionsByUser}")
    public InstitutionResourceIC getInstitutionsFromInfocamere(Principal principal) {
        log.trace("getInstitutionsFromInfocamere start");

        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) principal;
        SelfCareUser selfCareUser = (SelfCareUser) jwtAuthenticationToken.getPrincipal();

        InstitutionInfoIC institutionInfoIC = institutionService.getInstitutionsByUser(selfCareUser.getFiscalCode());
        InstitutionResourceIC institutionResourceIC = InstitutionMapper.toResource(institutionInfoIC);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionsFromInfocamere result = {}", institutionResourceIC);
        log.trace("getInstitutionsFromInfocamere end");
        return institutionResourceIC;
    }

    @PostMapping(value = "/{externalInstitutionId}/match")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.matchInstitutionAndUser}")
    public MatchInfoResultResource matchInstitutionAndUser(@ApiParam("${swagger.onboarding.institutions.model.externalId}")
                                                     @PathVariable("externalInstitutionId")
                                                     String externalInstitutionId,
                                                           @RequestBody
                                                     @Valid
                                                     UserDto userDto) {
        log.trace("matchInstitutionAndUser start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "matchInstitutionAndUser userDto = {}", userDto);
        MatchInfoResult matchInfoResult = institutionService.matchInstitutionAndUser(externalInstitutionId, UserMapper.toUser(userDto));
        MatchInfoResultResource result = InstitutionMapper.toResource(matchInfoResult);
        log.debug("matchInstitutionAndUser result = {}", result);
        log.trace("matchInstitutionAndUser end");
        return result;
    }

    @GetMapping(value = "/{externalInstitutionId}/legal-address")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.getInstitutionLegalAddress}")
    public InstitutionLegalAddressResource getInstitutionLegalAddress(@ApiParam("${swagger.onboarding.institutions.model.externalId}")
                                                                          @PathVariable("externalInstitutionId")
                                                                          String externalInstitutionId) {
        log.trace("getInstitutionLegalAddress start");
        log.debug("getInstitutionLegalAddress institutionId = {}", externalInstitutionId);
        InstitutionLegalAddressData institutionLegalAddressData = institutionService.getInstitutionLegalAddress(externalInstitutionId);
        InstitutionLegalAddressResource result = OnboardingMapper.toResource(institutionLegalAddressData);
        log.debug("getInstitutionLegalAddress result = {}", result);
        log.trace("getInstitutionLegalAddress end");
        return result;
    }

}
