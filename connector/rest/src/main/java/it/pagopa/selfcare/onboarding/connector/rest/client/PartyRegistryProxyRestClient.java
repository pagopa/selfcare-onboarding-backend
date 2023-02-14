package it.pagopa.selfcare.onboarding.connector.rest.client;

import it.pagopa.selfcare.onboarding.connector.model.PnPGInstitutionLegalAddressData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionPnPGInfo;
import it.pagopa.selfcare.onboarding.connector.rest.model.institution_pnpg.InstitutionByLegalTaxIdRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Party Registry Proxy Rest Client
 */
@FeignClient(name = "${rest-client.party-registry-proxy.serviceCode}", url = "${rest-client.party-registry-proxy.base-url}")
public interface PartyRegistryProxyRestClient {

    @PostMapping(value = "${rest-client.party-registry-proxy.getInstitutionsByUserLegalTaxId.path}", consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    InstitutionPnPGInfo getInstitutionsByUserLegalTaxId(@RequestBody InstitutionByLegalTaxIdRequest request);

    @GetMapping(value = "${rest-client.party-registry-proxy.getInstitutionLegalAddress.path}", consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    PnPGInstitutionLegalAddressData getInstitutionLegalAddress(@RequestParam(value = "taxId", required = true) String externalInstitutionId);

}
