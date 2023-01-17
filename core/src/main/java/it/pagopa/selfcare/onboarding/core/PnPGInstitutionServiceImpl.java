package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.onboarding.connector.api.PartyRegistryProxyConnector;
import it.pagopa.selfcare.onboarding.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionPnPGInfo;
import it.pagopa.selfcare.onboarding.connector.model.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

import static it.pagopa.selfcare.onboarding.connector.model.user.User.Fields.fiscalCode;

@Slf4j
@Service
class PnPGInstitutionServiceImpl implements PnPGInstitutionService {

    private static final EnumSet<it.pagopa.selfcare.onboarding.connector.model.user.User.Fields> USER_FIELD_ONLY_FISCAL_CODE = EnumSet.of(fiscalCode);

    private final PartyRegistryProxyConnector partyRegistryProxyConnector;
    private final UserRegistryConnector userConnector;


    @Autowired
    PnPGInstitutionServiceImpl(PartyRegistryProxyConnector partyRegistryProxyConnector,
                               UserRegistryConnector userConnector) {
        this.partyRegistryProxyConnector = partyRegistryProxyConnector;
        this.userConnector = userConnector;
    }

    @Override
    public InstitutionPnPGInfo getInstitutionsByUserId(String userId) {
        log.trace("getInstitutionsByUserId start");
        log.debug("getInstitutionsByUserId userId = {}", userId);
//        User user = userConnector.getUserByInternalId(userId, USER_FIELD_ONLY_FISCAL_CODE);
        User user = new User();
        if (userId.equals("123456")) // fixme: remove after InfoCamere has been integrated
            user.setFiscalCode("CF1");
        else if (userId.equals("654321"))
            user.setFiscalCode("CF2");
        else
            user.setFiscalCode("NoCF");
        InstitutionPnPGInfo result = partyRegistryProxyConnector.getInstitutionsByUserFiscalCode(user.getFiscalCode());
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionsByUserId result = {}", result);
        log.trace("getInstitutionsByUserId end");
        return result;
    }

}
