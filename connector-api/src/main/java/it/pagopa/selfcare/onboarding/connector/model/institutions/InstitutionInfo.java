package it.pagopa.selfcare.onboarding.connector.model.institutions;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.Billing;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionType;
import lombok.Data;

import java.util.Objects;

@Data
public class InstitutionInfo {

    private String id;
    private String externalId;
    private String description;
    private String status;
    private String taxCode;
    private String address;
    private String digitalAddress;
    private String pricingPlan;
    private String zipCode;
    private String category;
    private Billing billing;
    private String origin;
    private String originId;
    private InstitutionType institutionType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstitutionInfo that = (InstitutionInfo) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
