package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.institutions.MatchInfoResult;
import it.pagopa.selfcare.onboarding.connector.model.institutions.infocamere.BusinessInfoIC;
import it.pagopa.selfcare.onboarding.connector.model.institutions.infocamere.InstitutionInfoIC;
import it.pagopa.selfcare.onboarding.web.model.BusinessResourceIC;
import it.pagopa.selfcare.onboarding.web.model.InstitutionResource;
import it.pagopa.selfcare.onboarding.web.model.InstitutionResourceIC;
import it.pagopa.selfcare.onboarding.web.model.MatchInfoResultResource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;
import java.util.stream.Collectors;

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

    public static InstitutionResourceIC toResource(InstitutionInfoIC model) {
        InstitutionResourceIC resource = null;
        if (model != null) {
            resource = new InstitutionResourceIC();

            resource.setLegalTaxId(model.getLegalTaxId());
            resource.setRequestDateTime(model.getRequestDateTime());
            resource.setBusinesses(model.getBusinesses()
                    .stream()
                    .map(InstitutionMapper::toResource)
                    .collect(Collectors.toList()));
        }
        return resource;
    }

    public static BusinessResourceIC toResource(BusinessInfoIC model) {
        BusinessResourceIC resource = null;
        if (model != null) {
            resource = new BusinessResourceIC();

            resource.setBusinessName(model.getBusinessName());
            resource.setBusinessTaxId(model.getBusinessTaxId());
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
            resource.setParentDescription(model.getParentDescription());
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

    public static MatchInfoResultResource toResource(MatchInfoResult model) {
        MatchInfoResultResource resource = null;
        if (model != null) {
            resource = new MatchInfoResultResource();

            resource.setVerificationResult(model.isVerificationResult());
        }
        return resource;
    }



}
