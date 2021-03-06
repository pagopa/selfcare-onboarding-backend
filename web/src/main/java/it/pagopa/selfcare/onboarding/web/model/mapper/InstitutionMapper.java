package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.web.model.InstitutionResource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InstitutionMapper {

    public static InstitutionResource toResource(InstitutionInfo model) {
        InstitutionResource resource = null;
        if (model != null) {
            resource = new InstitutionResource();
            if (model.getId() != null) {
                resource.setId(UUID.fromString(model.getId()));
            }
            resource.setDescription(model.getDescription());
            resource.setExternalId(model.getExternalId());
            resource.setAddress(model.getAddress());
            resource.setDigitalAddress(model.getDigitalAddress());
            resource.setTaxCode(model.getTaxCode());
            resource.setZipCode(model.getZipCode());
            resource.setOrigin(model.getOrigin());
            if (model.getUserRole() != null) {
                resource.setUserRole(model.getUserRole().getSelfCareAuthority());
            }
        }
        return resource;
    }


    public static InstitutionResource toResource(Institution model) {
        InstitutionResource resource = null;
        if (model != null) {
            resource = new InstitutionResource();
            if (model.getId() != null) {
                resource.setId(UUID.fromString(model.getId()));
            }
            resource.setDescription(model.getDescription());
            resource.setExternalId(model.getExternalId());
            resource.setAddress(model.getAddress());
            resource.setOriginId(model.getOriginId());
            resource.setInstitutionType(model.getInstitutionType());
            resource.setDigitalAddress(model.getDigitalAddress());
            resource.setTaxCode(model.getTaxCode());
            resource.setZipCode(model.getZipCode());
            resource.setOrigin(model.getOrigin());
        }
        return resource;
    }

}
