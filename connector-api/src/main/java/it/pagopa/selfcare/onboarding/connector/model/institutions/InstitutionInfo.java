package it.pagopa.selfcare.onboarding.connector.model.institutions;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.BillingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionType;
import lombok.Data;

import java.util.Objects;

@Data
public class InstitutionInfo {

    private String institutionId;
    private String description;
    private String taxCode;
    private String digitalAddress;
    private String status;
    private String address;
    private String category;
    private String zipCode;
    private BillingData billing;
    private String origin;
    private InstitutionType institutionType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstitutionInfo that = (InstitutionInfo) o;
        return institutionId.equals(that.institutionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(institutionId);
    }
}
