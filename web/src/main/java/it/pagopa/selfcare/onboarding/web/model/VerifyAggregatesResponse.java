package it.pagopa.selfcare.onboarding.web.model;

import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import lombok.Data;

import java.util.List;

@Data
public class VerifyAggregatesResponse {
    private List<Institution> aggregates ;
    private List<RowErrorResponse> errors;
}
