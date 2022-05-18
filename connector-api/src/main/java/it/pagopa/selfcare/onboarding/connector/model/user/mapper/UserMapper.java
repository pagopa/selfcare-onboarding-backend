package it.pagopa.selfcare.onboarding.connector.model.user.mapper;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.onboarding.connector.model.user.SaveUserDto;
import it.pagopa.selfcare.onboarding.connector.model.user.WorkContact;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

    public static SaveUserDto toSaveUserDto(User model, String institutionId) {
        SaveUserDto resource = null;
        if (model != null) {
            resource = new SaveUserDto();
            resource.setFiscalCode(model.getTaxCode());
            resource.setName(CertifiedFieldMapper.map(model.getName()));
            resource.setFamilyName(CertifiedFieldMapper.map(model.getSurname()));
            if (institutionId != null) {
                WorkContact contact = new WorkContact();
                contact.setEmail(CertifiedFieldMapper.map(model.getEmail()));
                resource.setWorkContacts(Map.of(institutionId, contact));
            }
        }
        return resource;
    }

}
