package it.pagopa.selfcare.onboarding.connector.rest.model;

import it.pagopa.selfcare.onboarding.connector.model.Certification;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionContact;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * PersonInfo
 */
@Data
public class PersonInfo {

    private String name;
    private String surname;
    private String taxCode;
    private Certification certification;
    private Map<String, List<InstitutionContact>> institutionContacts;


}
