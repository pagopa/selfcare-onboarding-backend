package it.pagopa.selfcare.onboarding.connector.model.user;

import lombok.Data;

@Data
public class SaveUserDto extends MutableUserFieldsDto {

    private String fiscalCode;

}
