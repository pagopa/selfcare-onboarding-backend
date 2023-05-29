package it.pagopa.selfcare.onboarding.connector.rest.client;

import it.pagopa.selfcare.onboarding.connector.model.InstitutionLegalAddressData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.MatchInfoResult;
import it.pagopa.selfcare.onboarding.connector.model.institutions.infocamere.InstitutionInfoIC;
import it.pagopa.selfcare.onboarding.connector.rest.model.institution_pnpg.InstitutionByLegalTaxIdRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Party Registry Proxy Rest Client
 */
@FeignClient(name = "${rest-client.party-registry-proxy.serviceCode}", url = "${rest-client.party-registry-proxy.base-url}")
public interface PartyRegistryProxyRestClient {

    @PostMapping(value = "${rest-client.party-registry-proxy.getInstitutionsByUserLegalTaxId.path}", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    InstitutionInfoIC getInstitutionsByUserLegalTaxId(@RequestBody InstitutionByLegalTaxIdRequest request);

    @GetMapping(value = "${rest-client.party-registry-proxy.matchInstitutionAndUser.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    MatchInfoResult matchInstitutionAndUser(@RequestParam(value = "vatNumber") String institutionExternalId,
                                            @RequestParam(value = "taxId") String taxCode);

    @GetMapping(value = "${rest-client.party-registry-proxy.getInstitutionLegalAddress.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    InstitutionLegalAddressData getInstitutionLegalAddress(@RequestParam(value = "taxId") String externalInstitutionId);

}
