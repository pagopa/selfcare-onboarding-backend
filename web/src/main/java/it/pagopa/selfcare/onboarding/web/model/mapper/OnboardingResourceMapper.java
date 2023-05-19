package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.web.model.OnboardingSubunitDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OnboardingResourceMapper {

    OnboardingData toEntity(OnboardingSubunitDto dto);
}
