package it.pagopa.selfcare.onboarding.connector.model.institutions;

import it.pagopa.selfcare.onboarding.connector.model.BusinessPnPG;
import lombok.Data;

import java.util.List;

@Data
public class InstitutionPnPGInfo {

    private String legalTaxId;
    private String requestDateTime;
    private List<BusinessPnPG> businesses;

}
