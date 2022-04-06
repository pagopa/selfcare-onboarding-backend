package it.pagopa.selfcare.onboarding.connector.model.onboarding;

import lombok.Data;

@Data
public class UserInfo {

    private String name;
    private String surname;
    private String taxCode;
    private String from;
    private String email;
    private PartyRole role;

}
