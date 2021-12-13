package it.pagopa.selfcare.onboarding.connector.model.product;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.PartyRole;
import lombok.Getter;
import lombok.Setter;

import java.util.EnumMap;
import java.util.List;

@Setter
@Getter
public class Product {

    private String id;
    private EnumMap<PartyRole, List<String>> roleMappings;
    private String contractTemplatePath;
    private String contractTemplateVersion;

}
