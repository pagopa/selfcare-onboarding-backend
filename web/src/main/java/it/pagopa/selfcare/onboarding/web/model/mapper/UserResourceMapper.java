package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.onboarding.web.model.ManagerInfoResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserResourceMapper {
    ManagerInfoResponse toManagerInfoResponse(User user);
}
