package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.PnPGInstitutionLegalAddressData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.Billing;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionType;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionUpdate;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PnPGOnboardingData;
import it.pagopa.selfcare.onboarding.web.model.PnPGInstitutionLegalAddressResource;
import it.pagopa.selfcare.onboarding.web.model.PnPGOnboardingDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PnPGOnboardingMapper {

    public static PnPGOnboardingData toOnboardingData(String externalId, String productId, PnPGOnboardingDto model) {
        PnPGOnboardingData resource = null;
        if (model != null) {
            resource = new PnPGOnboardingData();
            resource.setUsers(model.getUsers().stream()
                    .map(PnPGUserMapper::toUser)
                    .collect(Collectors.toList()));
            resource.setInstitutionExternalId(externalId);
            resource.setBusinessName(model.getBillingData().getBusinessName());
            resource.setProductId(productId);
            resource.setInstitutionUpdate(mockInstitutionUpdate(externalId)); // fixme
            if (model.getBillingData() != null) {
                resource.setBillingRequest(mockBillingData(externalId)); // fixme
            }
            resource.setInstitutionType(InstitutionType.PG);
            if (model.getBillingData().getBusinessName().equals("")) {
                resource.setExistsInRegistry(false);
            }
        }
        return resource;
    }

    private static InstitutionUpdate mockInstitutionUpdate(String externalId) {
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setInstitutionType(InstitutionType.PG);
        institutionUpdate.setTaxCode(externalId);
        return institutionUpdate;
    }

    private static Billing mockBillingData(String externalId) {
        Billing billingData = new Billing();
        billingData.setVatNumber(externalId);
        billingData.setRecipientCode("");
        billingData.setPublicServices(false);
        return billingData;
    }

    public static PnPGInstitutionLegalAddressResource toResource(PnPGInstitutionLegalAddressData model) {
        PnPGInstitutionLegalAddressResource resource = null;
        if (model != null) {
            resource = new PnPGInstitutionLegalAddressResource();

            resource.setAddress(model.getAddress());
            resource.setZipCode(model.getZipCode());
        }
        return resource;
    }

}
