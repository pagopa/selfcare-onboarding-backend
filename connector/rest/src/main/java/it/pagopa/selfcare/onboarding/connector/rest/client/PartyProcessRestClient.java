package it.pagopa.selfcare.onboarding.connector.rest.client;

import it.pagopa.selfcare.onboarding.connector.rest.model.OnboardingRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Party Process Rest Client
 */
@FeignClient(name = "${rest-client.party-process.serviceCode}", url = "${rest-client.party-process.base-url}")
public interface PartyProcessRestClient {

    @PostMapping(value = "${rest-client.party-process.onboardingOrganization.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    void onboardingOrganization(@RequestBody OnboardingRequest request);

}
