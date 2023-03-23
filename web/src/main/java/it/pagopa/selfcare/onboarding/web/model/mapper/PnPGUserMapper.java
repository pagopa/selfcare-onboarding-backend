package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.onboarding.web.model.UserDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PnPGUserMapper {


    public static User toUser(UserDto model) {
        User resource = null;
        if (model != null) {
            resource = new User();
            resource.setRole(PartyRole.MANAGER);
            resource.setName(model.getName());
            resource.setSurname(model.getSurname());
            resource.setTaxCode(model.getTaxCode());
        }
        return resource;
    }

}
