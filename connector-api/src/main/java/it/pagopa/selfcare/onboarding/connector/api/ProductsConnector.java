package it.pagopa.selfcare.onboarding.connector.api;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.PartyRole;

import java.util.EnumMap;
import java.util.List;

public interface ProductsConnector {

    EnumMap<PartyRole, List<String>> getProductRoleMappings(String id);

}
