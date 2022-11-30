package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.GeographicTaxonomy;
import it.pagopa.selfcare.onboarding.web.model.GeographicTaxonomyDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GeographicTaxonomyMapper {


    public static GeographicTaxonomy toGeographicTaxonomy (GeographicTaxonomyDto model) {
        GeographicTaxonomy resource = null;
        if (model != null) {
            resource = new GeographicTaxonomy();
            resource.setCode(model.getCode());
            resource.setDesc(model.getDesc());
        }
        return resource;
    }
}
