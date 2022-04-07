package it.pagopa.selfcare.onboarding.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
import it.pagopa.selfcare.onboarding.core.InstitutionService;
import it.pagopa.selfcare.onboarding.web.model.*;
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
//
//        OnboardingResponse result = OnboardingMapper.toResource(institutionService.onboarding(new OnboardingData(institutionId,
//                productId,
//                request.getUsers(),
//                request.getBillingData(),
//                request.getOrganizationType())));
//        log.debug("onboarding result = {}", result);
        log.trace("onboarding end");
        return null;
    }

    //TODO @PreAuthorize("hasPermission(#institutionId, 'InstitutionResource', 'ADMIN')")
    @GetMapping(value = "/{institutionId}/products/{productId}/onboarded-institution-info")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.getInstitutionOnboardingInfo}")
    public InstitutionOnboardingInfoResource getInstitutionOnboardingInfo(@ApiParam("${swagger.onboarding.institutions.model.id}")
                                                                          @PathVariable("institutionId")
                                                                                  String institutionId,
                                                                          @ApiParam("${swagger.onboarding.products.model.id}")
                                                                          @PathVariable("productId")
                                                                                  String productId) {
        UserInfo manager = institutionService.getManager(institutionId, productId);
        //TODO for now retrieve just the Manager

        return null;
    }

    @GetMapping(value = "/{institutionId}/data")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.manager}")
    public InstitutionData getInstitutionData(@ApiParam("${swagger.onboarding.institutions.model.id}")
                                              @PathVariable("institutionId")
                                                      String institutionId) {

        return null;
    }


    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutions}")
    public List<InstitutionResource> getInstitutions() {//TODO se party restituisce i billingData in questa response potremmo decidere di resituirli direttamente qua al FE
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
