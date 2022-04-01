package it.pagopa.selfcare.onboarding.connector;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.onboarding.connector.api.PartyConnector;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipsResponse;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingResource;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingResponseData;
import it.pagopa.selfcare.onboarding.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnBoardingInfo;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnboardingContract;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnboardingRequest;
import it.pagopa.selfcare.onboarding.connector.rest.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.onboarding.connector.model.RelationshipState.ACTIVE;

@Service
@Slf4j
class PartyConnectorImpl implements PartyConnector {

    private final PartyProcessRestClient restClient;
    private static final BinaryOperator<InstitutionInfo> MERGE_FUNCTION =
            (inst1, inst2) -> ACTIVE.name().equals(inst1.getStatus()) ? inst1 : inst2;
    private static final Function<OnboardingResponseData, InstitutionInfo> ONBOARDING_DATA_TO_INSTITUTION_INFO_FUNCTION = onboardingData -> {
        InstitutionInfo institutionInfo = new InstitutionInfo();
        institutionInfo.setInstitutionId(onboardingData.getInstitutionId());
        institutionInfo.setDescription(onboardingData.getDescription());
        institutionInfo.setStatus(onboardingData.getState().toString());
        return institutionInfo;
    };

    @Autowired
    public PartyConnectorImpl(PartyProcessRestClient restClient) {
        this.restClient = restClient;
    }


    @Override
    public OnboardingResource onboardingOrganization(OnboardingData onboardingData) {
        Assert.notNull(onboardingData, "Onboarding data is required");
        OnboardingRequest onboardingRequest = new OnboardingRequest();
        onboardingRequest.setInstitutionId(onboardingData.getInstitutionId());
        //TODO onboardingRequest.setDatiFatturazione(onboardingData.getDatiFatturazione());
        onboardingRequest.setUsers(onboardingData.getUsers().stream()
                .map(userInfo -> {
                    User user = new User();
                    user.setProduct(onboardingData.getProductId());
                    user.setName(userInfo.getName());
                    user.setSurname(userInfo.getSurname());
                    user.setTaxCode(userInfo.getTaxCode());
                    user.setEmail(userInfo.getEmail());
                    user.setRole(userInfo.getRole());
                    user.setProductRole(userInfo.getProductRole());
                    return user;
                }).collect(Collectors.toList()));
        OnboardingContract onboardingContract = new OnboardingContract();
        onboardingContract.setPath(onboardingData.getContractPath());
        onboardingContract.setVersion(onboardingData.getContractVersion());
        onboardingRequest.setContract(onboardingContract);

        return restClient.onboardingOrganization(onboardingRequest);
    }

    @Override
    public Collection<InstitutionInfo> getOnBoardedInstitutions() {
        log.trace("getOnBoardedInstitutions start");
        OnBoardingInfo onBoardingInfo = restClient.getOnBoardingInfo(null, EnumSet.of(ACTIVE));
        Collection<InstitutionInfo> result = parseOnBoardingInfo(onBoardingInfo);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getOnBoardedInstitutions result = {}", result);
        log.trace("getOnBoardedInstitutions end");
        return result;
    }


    private Collection<InstitutionInfo> parseOnBoardingInfo(OnBoardingInfo onBoardingInfo) {
        log.trace("parseOnBoardingInfo start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "parseOnBoardingInfo onBoardingInfo = {}", onBoardingInfo);
        Collection<InstitutionInfo> institutions = Collections.emptyList();
        if (onBoardingInfo != null && onBoardingInfo.getInstitutions() != null) {
            institutions = onBoardingInfo.getInstitutions().stream()
                    .map(ONBOARDING_DATA_TO_INSTITUTION_INFO_FUNCTION)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(InstitutionInfo::getInstitutionId, Function.identity(), MERGE_FUNCTION),
                            Map::values
                    ));
        }
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "parseOnBoardingInfo result = {}", institutions);
        log.trace("parseOnBoardingInfo end");
        return institutions;
    }

    @Override
    public RelationshipsResponse getUserInstitutionRelationships(String institutionId, String productId) {
        log.trace("getUserInstitutionRelationships start");
        log.debug("getUserInstitutionRelationships institutionId = {}, productId = {}", institutionId, productId);
        Assert.notNull(institutionId, "An institutionId is required");
        Assert.notNull(productId, "A productId is required");
        RelationshipsResponse institutionRelationships = restClient.getUserInstitutionRelationships(
                institutionId,
                null,
                EnumSet.of(ACTIVE),
                Set.of(productId),
                null,
                null
        );
        log.debug("getUserInstitutionRelationships institutionRelationships = {}", institutionRelationships);
        log.trace("getUserInstitutionRelationships end");
        return institutionRelationships;
    }


}
