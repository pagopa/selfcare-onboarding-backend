package it.pagopa.selfcare.onboarding.connector.model.institutions;

import lombok.Data;

@Data
public class ManagerVerification {
    private String origin;
    private String companyName;

    public ManagerVerification() {
    }

    public ManagerVerification(String origin, String companyName) {
        this.origin = origin;
        this.companyName = companyName;
    }
}
