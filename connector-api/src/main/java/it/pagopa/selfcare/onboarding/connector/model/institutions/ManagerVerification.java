package it.pagopa.selfcare.onboarding.connector.model.institutions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManagerVerification {
    private String origin;
    private String companyName;
}