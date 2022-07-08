package it.pagopa.selfcare.onboarding.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.core.InstitutionService;
import it.pagopa.selfcare.onboarding.web.model.InstitutionOnboardingInfoResource;
import it.pagopa.selfcare.onboarding.web.model.InstitutionResource;
import it.pagopa.selfcare.onboarding.web.model.OnboardingDto;
import it.pagopa.selfcare.onboarding.web.model.mapper.InstitutionMapper;
import it.pagopa.selfcare.onboarding.web.model.mapper.OnboardingMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/institutions", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "institutions")
public class InstitutionController {

    private final InstitutionService institutionService;


    @Autowired
    public InstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }


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
        institutionService.onboarding(OnboardingMapper.toOnboardingData(externalInstitutionId, productId, request));
        log.trace("onboarding end");
    }

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

    @Deprecated
    @GetMapping(value = "/{externalInstitutionId}/data")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.getInstitutionData}")
    public InstitutionResource getInstitutionData(@ApiParam("${swagger.onboarding.institutions.model.externalId}")
                                                  @PathVariable("externalInstitutionId")
                                                          String externalInstitutionId) {
        log.trace("getInstitutionData start");
        log.debug("getInstitutionData institutionId = {}", externalInstitutionId);
        Institution institution = institutionService.getInstitutionByExternalId(externalInstitutionId);
        InstitutionResource result = InstitutionMapper.toResource(institution);
        log.debug("getInstitutionData result = {}", result);
        log.trace("getInstitutionData end");
        return result;
    }


    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.getInstitutions}")
    public List<InstitutionResource> getInstitutions() {
        log.trace("getInstitutions start");
        List<InstitutionResource> institutionResources = institutionService.getInstitutions()
                .stream()
                .map(InstitutionMapper::toResource)
                .collect(Collectors.toList());
        log.debug("getInstitutions result = {}", institutionResources);
        log.trace("getInstitutions end");
        return institutionResources;
    }

}
