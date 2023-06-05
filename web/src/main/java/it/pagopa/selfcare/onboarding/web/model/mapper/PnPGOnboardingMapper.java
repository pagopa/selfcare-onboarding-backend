package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.InstitutionLegalAddressData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.Billing;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionType;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionUpdate;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.PnPGOnboardingData;
import it.pagopa.selfcare.onboarding.web.model.InstitutionLegalAddressResource;
import it.pagopa.selfcare.onboarding.web.model.PnPGOnboardingDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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
            resource.setInstitutionUpdate(fillInstitutionUpdate(model));
            if (model.getBillingData() != null) {
                resource.setBillingRequest(fillBillingData(externalId));
            }
            resource.setInstitutionType(InstitutionType.PG);
            resource.setExistsInRegistry(model.getBillingData().isCertified());
            resource.setDigitalAddress(model.getBillingData().getDigitalAddress());
        }
        return resource;
    }

    private static InstitutionUpdate fillInstitutionUpdate(PnPGOnboardingDto model) {
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setInstitutionType(InstitutionType.PG);
        institutionUpdate.setTaxCode(model.getBillingData().getTaxCode());
        institutionUpdate.setDescription(model.getBillingData().getBusinessName());
        institutionUpdate.setDigitalAddress(model.getBillingData().getDigitalAddress());
        institutionUpdate.setGeographicTaxonomies(new ArrayList<>());
        return institutionUpdate;
    }

    private static Billing fillBillingData(String externalId) {
        Billing billingData = new Billing();
        billingData.setVatNumber(externalId);
        billingData.setPublicServices(false);
        return billingData;
    }

    public static InstitutionLegalAddressResource toResource(InstitutionLegalAddressData model) {
        InstitutionLegalAddressResource resource = null;
        if (model != null) {
            resource = new InstitutionLegalAddressResource();

            resource.setAddress(model.getAddress());
            resource.setZipCode(model.getZipCode());
        }
        return resource;
    }

}
