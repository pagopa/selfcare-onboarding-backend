package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
import it.pagopa.selfcare.onboarding.connector.model.user.CertifiedField;
import it.pagopa.selfcare.onboarding.connector.model.user.WorkContact;
import it.pagopa.selfcare.onboarding.web.model.UserDataValidationDto;
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
            resource.setName(model.getName());
            resource.setSurname(model.getSurname());
            resource.setProductRole(model.getProductRole());
            resource.setTaxCode(model.getTaxCode());
            resource.setEmail(model.getEmail());
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
                resource.setName(model.getUser().getName().getValue());
                resource.setTaxCode(model.getUser().getFiscalCode());
                resource.setSurname(model.getUser().getFamilyName().getValue());
                Optional.ofNullable(model.getUser().getWorkContacts())
                        .map(map -> map.get(model.getInstitutionId()))
                        .map(WorkContact::getEmail)
                        .map(CertifiedField::getValue)
                        .ifPresent(resource::setEmail);
            }
        }
        return resource;
    }


    public static User toUser(UserDataValidationDto model) {
        User resource = null;
        if (model != null) {
            resource = new User();
            resource.setTaxCode(model.getTaxCode());
            resource.setName(model.getName());
            resource.setSurname(model.getSurname());
        }
        return resource;
    }

}
