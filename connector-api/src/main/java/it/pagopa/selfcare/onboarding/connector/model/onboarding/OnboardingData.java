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

package it.pagopa.selfcare.onboarding.connector.model.onboarding;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
public class OnboardingData {

    private String institutionExternalId;
    private String taxCode;
    private String subunitCode;
    private String subunitType;
    private String productId;
    private String productName;
    private List<User> users;
    private String contractPath;
    private String contractVersion;
    private Billing billing;
    private InstitutionUpdate institutionUpdate;
    private InstitutionType institutionType;
    private String origin;
    private String pricingPlan;

    public List<User> getUsers() {
        return Optional.ofNullable(users).orElse(Collections.emptyList());
    }

}
