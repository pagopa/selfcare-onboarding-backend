package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.onboarding.web.model.ManagerInfoResponse;
import it.pagopa.selfcare.onboarding.web.model.UserTaxCodeDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserResourceMapper {
    ManagerInfoResponse toManagerInfoResponse(User user);

    default String toString(UserTaxCodeDto userTaxCode) {
        return userTaxCode != null ? userTaxCode.getTaxCode() : null;
    }
}
