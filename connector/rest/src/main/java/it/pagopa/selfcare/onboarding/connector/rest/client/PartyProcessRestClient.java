package it.pagopa.selfcare.onboarding.connector.rest.client;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipInfo;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipState;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipsResponse;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.rest.model.BillingDataResponse;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnBoardingInfo;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnboardingInstitutionRequest;
import org.springframework.cloud.openfeign.CollectionFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.Set;

import static feign.CollectionFormat.CSV;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;

/**
 * Party Process Rest Client
 */
@FeignClient(name = "${rest-client.party-process.serviceCode}", url = "${rest-client.party-process.base-url}")
public interface PartyProcessRestClient {

    @GetMapping(value = "${rest-client.party-process.getUserInstitutionRelationships.path}", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    @CollectionFormat(CSV)
    RelationshipsResponse getUserInstitutionRelationships(@PathVariable("externalId") String institutionId,
                                                          @RequestParam(value = "roles", required = false) EnumSet<PartyRole> roles,
                                                          @RequestParam(value = "states", required = false) EnumSet<RelationshipState> states,
                                                          @RequestParam(value = "products", required = false) Set<String> productIds,
                                                          @RequestParam(value = "productRoles", required = false) Set<String> productRoles,
                                                          @RequestParam(value = "personId", required = false) String personId);

    @PostMapping(value = "${rest-client.party-process.onboardingOrganization.path}", consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    void onboardingOrganization(@RequestBody OnboardingInstitutionRequest request);


    @GetMapping(value = "${rest-client.party-process.getOnBoardingInfo.path}", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    @CollectionFormat(CSV)
    OnBoardingInfo getOnBoardingInfo(@RequestParam(value = "institutionExternalId", required = false) String institutionExternalId,
                                     @RequestParam(value = "states", required = false) EnumSet<RelationshipState> states);

    @GetMapping(value = "${rest-client.party-process.getInstitutionByExternalId.path}", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    Institution getInstitutionByExternalId(@PathVariable("externalId") String externalId);

    @PostMapping(value = "${rest-client.party-process.createInstitutionUsingExternalId.path}", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    Institution createInstitutionUsingExternalId(@PathVariable("externalId") String externalId);

    @GetMapping(value = "${rest-client.party-process.getInstitutionManager.path}", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    RelationshipInfo getInstitutionManager(@PathVariable("externalId") String externalId,
                                           @PathVariable("productId") String productId);

    @GetMapping(value = "${rest-client.party-process.getInstitutionBillingData.path}", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    BillingDataResponse getInstitutionBillingData(@PathVariable("externalId") String externalId,
                                                  @PathVariable("productId") String productId);

    @RequestMapping(method = HEAD, value = "${rest-client.party-process.verifyOnboarding.path}")
    @ResponseBody
    void verifyOnboarding(@PathVariable("externalId") String externalInstitutionId,
                          @PathVariable("productId") String productId);

}
