package it.pagopa.selfcare.onboarding.web.model;

import lombok.Data;

@Data
public class RowErrorResponse {
    private Integer row;
    private String cf;
    private String reason;
}
