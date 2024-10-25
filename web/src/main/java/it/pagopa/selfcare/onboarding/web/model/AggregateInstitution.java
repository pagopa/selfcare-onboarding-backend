package it.pagopa.selfcare.onboarding.web.model;

import it.pagopa.selfcare.onboarding.common.Origin;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class AggregateInstitution {

    @NotNull(message = "taxCode is required")
    private String taxCode;

    @NotNull(message = "description is required")
    private String description;

    private String subunitCode;
    private String subunitType;
    private List<GeographicTaxonomyDto> geographicTaxonomies;
    private String address;
    private String zipCode;
    private String originId;
    private Origin origin;
    private List<UserDto> users;

    private String taxCodePT;
    private String iban;
    private String service;
    private String syncAsyncMode;
    private String recipientCode;
    private String vatNumber;
    private String digitalAddress;
    private String city;
    private String county;
    private String parentDescription;



}
