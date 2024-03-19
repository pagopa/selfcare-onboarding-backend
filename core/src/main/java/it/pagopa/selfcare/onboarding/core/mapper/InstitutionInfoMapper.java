package it.pagopa.selfcare.onboarding.core.mapper;

import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;


@Mapper(componentModel = "spring")
public interface InstitutionInfoMapper {

    @Mappings({@Mapping(source = "city", target = "institutionLocation.city"),
            @Mapping(source = "county", target = "institutionLocation.county"),
            @Mapping(source = "country", target = "institutionLocation.country")})
    InstitutionInfo toInstitutionInfo(Institution institution);
}
