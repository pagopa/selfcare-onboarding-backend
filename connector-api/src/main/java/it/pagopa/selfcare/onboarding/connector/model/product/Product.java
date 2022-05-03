package it.pagopa.selfcare.onboarding.connector.model.product;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.PartyRole;
import lombok.Data;

import java.util.EnumMap;

@Data
public class Product {

    private String id;
    private EnumMap<PartyRole, ProductRoleInfo> roleMappings;
    private String contractTemplatePath;
    private String contractTemplateVersion;
    private String title;
    private String parentId;
}
