package it.pagopa.selfcare.onboarding.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.core.InstitutionService;
import it.pagopa.selfcare.onboarding.web.model.InstitutionResource;
import it.pagopa.selfcare.onboarding.web.model.OnboardingDto;
import it.pagopa.selfcare.onboarding.web.model.OnboardingResponse;
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
@RequestMapping(value = "/institutions", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "institutions")
public class InstitutionController {

    private final InstitutionService institutionService;


    @Autowired
    public InstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }


    @PostMapping(value = "/{institutionId}/products/{productId}/onboarding")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.onboarding}")
    public OnboardingResponse onboarding(@ApiParam("${swagger.onboarding.institutions.model.id}")
                                         @PathVariable("institutionId")
                                                 String institutionId,
                                         @ApiParam("${swagger.onboarding.products.model.id}")
                                         @PathVariable("productId")
                                                 String productId,
                                         @RequestBody
                                         @Valid
                                                 OnboardingDto request) {
        log.trace("onboarding start");
        log.debug("onboarding institutionId = {}, productId = {}, request = {}", institutionId, productId, request);

        OnboardingResponse result = OnboardingMapper.toResource(institutionService.onboarding(new OnboardingData(institutionId, productId, request.getUsers(), request.getBillingData())));
        log.debug("onboarding result = {}", result);
        log.trace("onboarding end");
        return result;
    }


    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutions}")
    public List<InstitutionResource> getInstitutions() {
        log.trace("getInstitutions start");
        List<InstitutionResource> institutionResources = institutionService.getInstitutions()
                .stream()
                .map(OnboardingMapper::toResource)
                .collect(Collectors.toList());
        log.debug("getInstitutions institutionResources = {}", institutionResources);
        log.trace("getInstitutions end");
        return institutionResources;
    }

}
