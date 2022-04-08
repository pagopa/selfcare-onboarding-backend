package it.pagopa.selfcare.onboarding.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.core.InstitutionService;
import it.pagopa.selfcare.onboarding.web.model.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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


    @PostMapping(value = "/{institutionId}/products/{productId}/onboarding")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.onboarding.institutions.api.onboarding}")
    public void onboarding(@ApiParam("${swagger.onboarding.institutions.model.id}")
                           @PathVariable("institutionId")
                                   String institutionId,
                           @ApiParam("${swagger.onboarding.products.model.id}")
                           @PathVariable("productId")
                                   String productId,
                           @RequestBody
                           @Valid
                                   List<UserDto> users) {
        if (log.isDebugEnabled()) {
            log.trace("InstitutionController.onboarding");
            log.debug("institutionId = {}, productId = {}, users = {}", institutionId, productId, users);
        }

        institutionService.onboarding(new OnboardingData(institutionId, productId, users));
    }

}
