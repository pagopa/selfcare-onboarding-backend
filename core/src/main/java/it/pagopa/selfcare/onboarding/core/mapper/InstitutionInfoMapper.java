package it.pagopa.selfcare.onboarding.core.mapper;

import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface InstitutionInfoMapper {

    InstitutionInfo toInstitutionInfo(Institution institution);
}
