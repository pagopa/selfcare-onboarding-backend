package it.pagopa.selfcare.onboarding.connector.model.institutions;

import lombok.Data;

import java.util.List;

@Data
public class VerifyAggregateResult {
    private List<Institution> aggregates ;
    private List<RowError> errors;
}
