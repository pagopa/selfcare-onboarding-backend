package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.AssistanceContacts;
import it.pagopa.selfcare.onboarding.connector.model.institutions.CompanyInformations;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.GeographicTaxonomy;
import it.pagopa.selfcare.onboarding.web.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface OnboardingInstitutionInfoMapper {
    @Mappings({
            @Mapping(target = "institution", source = "institution", qualifiedByName = "toData"),
            @Mapping(target = "geographicTaxonomies", expression = "java(mapGeographicTaxonomies(model.getGeographicTaxonomies()))"),
            @Mapping(target = "institution.companyInformations", source = "companyInformations"),
            @Mapping(target = "institution.assistanceContacts", source = "assistanceContacts")
    })
    InstitutionOnboardingInfoResource toResource(InstitutionOnboardingData model);

    @Named("mapGeographicTaxonomies")
    default List<GeographicTaxonomyResource> mapGeographicTaxonomies(List<GeographicTaxonomy> geographicTaxonomies) {
        return Optional.ofNullable(geographicTaxonomies)
                .map(geotaxes -> geotaxes.stream()
                        .map(GeographicTaxonomyMapper::toResource)
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    @Mappings({
            @Mapping(target = "billingData",  source = "model", qualifiedByName = "toBilling"),
            @Mapping(target = "city", source = "institutionLocation.city"),
            @Mapping(target = "country", source = "institutionLocation.country"),
            @Mapping(target = "county", source = "institutionLocation.county")
    })
    InstitutionData toData(InstitutionInfo model);

    @Named("toBilling")
    @Mappings({
            @Mapping(target = "publicServices", source = "billing.publicServices"),
            @Mapping(target = "recipientCode", source = "billing.recipientCode"),
            @Mapping(target = "vatNumber", source = "billing.vatNumber"),
    })
    BillingDataResponseDto toBilling(InstitutionInfo model);

    @Named("toInstitutionResource")
    @Mapping(target = "userRole", expression = "java(mapUserRole(model.getUserRole()))")
    @Mapping(target = "id", expression = "java(mapId(model.getId()))")
    InstitutionResource toResource(InstitutionInfo model);


    AssistanceContactsResource toResource(AssistanceContacts model);

    CompanyInformationsResource toResource(CompanyInformations model);

    @Named("mapUserRole")
    default SelfCareAuthority mapUserRole(PartyRole model) {
        // Map the userRole logic here
        return model != null ? model.getSelfCareAuthority() : null;
    }

    default UUID mapId(String id) {
        return id != null ? UUID.fromString(id) : null;
    }
}
