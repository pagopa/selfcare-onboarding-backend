/*
 * Party Process Micro Service
 * This service is the party process
 *
 * OpenAPI spec version: {{version}}
 * Contact: support@example.com
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package it.pagopa.selfcare.onboarding.connector.rest.model;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingResource;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Getter
@Setter
public class OnboardingResponse implements OnboardingResource {

    private String token;
    private File document;

}
