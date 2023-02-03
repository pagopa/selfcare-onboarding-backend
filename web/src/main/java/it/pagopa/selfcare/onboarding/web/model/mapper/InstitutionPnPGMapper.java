package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.BusinessPnPG;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionPnPGInfo;
import it.pagopa.selfcare.onboarding.web.model.BusinessPnPGResource;
import it.pagopa.selfcare.onboarding.web.model.InstitutionPnPGResource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InstitutionPnPGMapper {

    public static InstitutionPnPGResource toResource(InstitutionPnPGInfo model) {
        InstitutionPnPGResource resource = null;
        if (model != null) {
            resource = new InstitutionPnPGResource();

            resource.setLegalTaxId(model.getLegalTaxId());
            resource.setRequestDateTime(model.getRequestDateTime());
            resource.setBusinesses(model.getBusinesses()
                    .stream()
                    .map(InstitutionPnPGMapper::toResource)
                    .collect(Collectors.toList()));
        }
        return resource;
    }

    public static BusinessPnPGResource toResource(BusinessPnPG model) {
        BusinessPnPGResource resource = null;
        if (model != null) {
            resource = new BusinessPnPGResource();

            resource.setBusinessName(model.getBusinessName());
            resource.setBusinessTaxId(model.getBusinessTaxId());
        }

        return resource;
    }

}