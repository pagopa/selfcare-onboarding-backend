package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
import it.pagopa.selfcare.onboarding.connector.model.user.WorkContact;
import it.pagopa.selfcare.onboarding.web.model.UserDto;
import it.pagopa.selfcare.onboarding.web.model.UserResource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {


    public static User toUser(UserDto model) {
        User resource = null;
        if (model != null) {
            resource = new User();
            resource.setRole(model.getRole());
            resource.setEmail(model.getEmail());
            resource.setName(model.getName());
            resource.setSurname(model.getSurname());
            resource.setProductRole(model.getProductRole());
            resource.setTaxCode(model.getTaxCode());
        }
        return resource;
    }


    public static UserResource toResource(UserInfo model) {
        UserResource resource = null;
        if (model != null) {
            resource = new UserResource();
            resource.setId(UUID.fromString(model.getId()));
            resource.setRole(model.getRole());
            resource.setStatus(model.getStatus());
            resource.setInstitutionId(UUID.fromString(model.getInstitutionId()));
            if (model.getUser() != null) {
                resource.setName(CertifiedFieldMapper.map(model.getUser().getName()));
                resource.setTaxCode(model.getUser().getFiscalCode());
                resource.setSurname(CertifiedFieldMapper.map(model.getUser().getFamilyName()));
                resource.setEmail(Optional.ofNullable(model.getUser().getWorkContacts())
                        .map(map -> map.get(model.getInstitutionId()))
                        .map(WorkContact::getEmail)
                        .map(CertifiedFieldMapper::map)
                        .orElse(null));
            }
        }
        return resource;
    }

}
