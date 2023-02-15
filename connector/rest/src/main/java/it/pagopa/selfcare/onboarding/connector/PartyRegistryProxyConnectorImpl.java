package it.pagopa.selfcare.onboarding.connector;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.onboarding.connector.api.PartyRegistryProxyConnector;
import it.pagopa.selfcare.onboarding.connector.model.PnPGInstitutionLegalAddressData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionPnPGInfo;
import it.pagopa.selfcare.onboarding.connector.model.institutions.PnPGMatchInfo;
import it.pagopa.selfcare.onboarding.connector.rest.client.PartyRegistryProxyRestClient;
import it.pagopa.selfcare.onboarding.connector.rest.model.institution_pnpg.InstitutionByLegalTaxIdRequest;
import it.pagopa.selfcare.onboarding.connector.rest.model.institution_pnpg.InstitutionByLegalTaxIdRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@Slf4j
class PartyRegistryProxyConnectorImpl implements PartyRegistryProxyConnector {

    protected static final String REQUIRED_FISCAL_CODE_MESSAGE = "An user's fiscal code is required";
    private static final String REQUIRED_EXTERNAL_ID_MESSAGE = "An institution's external id is required ";

    private final PartyRegistryProxyRestClient restClient;


    @Autowired
    public PartyRegistryProxyConnectorImpl(PartyRegistryProxyRestClient restClient) {
        this.restClient = restClient;
    }


    @Override
    public InstitutionPnPGInfo getInstitutionsByUserFiscalCode(String taxCode) {
        log.trace("getInstitutionsByUserFiscalCode start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionsByUserFiscalCode taxCode = {}", taxCode);
        Assert.hasText(taxCode, REQUIRED_FISCAL_CODE_MESSAGE);
        InstitutionByLegalTaxIdRequestDto institutionByLegalTaxIdRequestDto = new InstitutionByLegalTaxIdRequestDto();
        institutionByLegalTaxIdRequestDto.setLegalTaxId(taxCode);
        InstitutionByLegalTaxIdRequest institutionByLegalTaxIdRequest = new InstitutionByLegalTaxIdRequest();
        institutionByLegalTaxIdRequest.setFilter(institutionByLegalTaxIdRequestDto);
        InstitutionPnPGInfo result = restClient.getInstitutionsByUserLegalTaxId(institutionByLegalTaxIdRequest);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionsByUserFiscalCode result = {}", result);
        log.trace("getInstitutionsByUserFiscalCode end");
        return result;
    }


    @Override
    public PnPGMatchInfo matchInstitutionAndUser(String externalInstitutionId, String taxCode) {
        log.trace("matchInstitutionAndUser start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "matchInstitutionAndUser taxCode = {}", taxCode);
        Assert.hasText(externalInstitutionId, REQUIRED_EXTERNAL_ID_MESSAGE);
        Assert.hasText(taxCode, REQUIRED_FISCAL_CODE_MESSAGE);
        PnPGMatchInfo result = restClient.matchInstitutionAndUser(taxCode, externalInstitutionId);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "matchInstitutionAndUser result = {}", result);
        log.trace("matchInstitutionAndUser end");
        return result;
    }

    @Override
    public PnPGInstitutionLegalAddressData getInstitutionLegalAddress(String externalInstitutionId) {
        log.trace("getInstitutionLegalAddress start");
        log.debug("getInstitutionLegalAddress externalInstitutionId = {}", externalInstitutionId);
        Assert.hasText(externalInstitutionId, REQUIRED_EXTERNAL_ID_MESSAGE);
        PnPGInstitutionLegalAddressData result = restClient.getInstitutionLegalAddress(externalInstitutionId);
        log.debug("getInstitutionLegalAddress result = {}", result);
        log.trace("getInstitutionLegalAddress end");
        return result;
    }

}
