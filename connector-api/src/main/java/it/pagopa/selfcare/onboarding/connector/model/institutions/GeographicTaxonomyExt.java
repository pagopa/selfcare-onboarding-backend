package it.pagopa.selfcare.onboarding.connector.model.institutions;

import lombok.Data;

import java.util.Date;

@Data
public class GeographicTaxonomyExt {
    private String code;
    private String desc;
    private String region;
    private String province;
    private String provinceAbbreviation;
    private String country;
    private String countryAbbreviation;
    private Date startDate;
    private Date endDate;
    private boolean enable;
}
