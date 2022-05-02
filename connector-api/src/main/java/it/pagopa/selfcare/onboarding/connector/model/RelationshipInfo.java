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

package it.pagopa.selfcare.onboarding.connector.model;


import it.pagopa.selfcare.onboarding.connector.model.onboarding.BillingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionUpdate;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PartyRole;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
public class RelationshipInfo {

    private String id;
    private String from;
    private String to;
    private String name;
    private String surname;
    private String taxCode;
    private Certification certification;
    //FIXME private Map<String, InstitutionContact> institutionContacts;
    private Map<String, List<InstitutionContact>> institutionContacts;
    private PartyRole role;
    private String pricingPlan;
    private RelationshipState state;
    private InstitutionUpdate institutionUpdate;
    private BillingData billing;
    private String email;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    //FIXME
    public String getEmail() {
        if (institutionContacts != null) {
            for (Map.Entry<String, List<InstitutionContact>> e : institutionContacts.entrySet()) {
                return e.getValue().get(0).getEmail();
            }
        }
        return null;
    }
}
