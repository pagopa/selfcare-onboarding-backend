package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.institutions.MatchInfoResult;
import it.pagopa.selfcare.onboarding.connector.model.institutions.infocamere.BusinessInfoIC;
import it.pagopa.selfcare.onboarding.connector.model.institutions.infocamere.InstitutionInfoIC;
import it.pagopa.selfcare.onboarding.web.model.BusinessResourceIC;
import it.pagopa.selfcare.onboarding.web.model.InstitutionResourceIC;
import it.pagopa.selfcare.onboarding.web.model.MatchInfoResultResource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PnPGInstitutionMapper {

    public static InstitutionResourceIC toResource(InstitutionInfoIC model) {
        InstitutionResourceIC resource = null;
        if (model != null) {
            resource = new InstitutionResourceIC();

            resource.setLegalTaxId(model.getLegalTaxId());
            resource.setRequestDateTime(model.getRequestDateTime());
            resource.setBusinesses(model.getBusinesses()
                    .stream()
                    .map(PnPGInstitutionMapper::toResource)
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

    public static MatchInfoResultResource toResource(MatchInfoResult model) {
        MatchInfoResultResource resource = null;
        if (model != null) {
            resource = new MatchInfoResultResource();

            resource.setVerificationResult(model.isVerificationResult());
        }
        return resource;
    }

}
