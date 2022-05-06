package it.pagopa.selfcare.onboarding.connector;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.onboarding.connector.api.PartyConnector;
import it.pagopa.selfcare.onboarding.connector.model.Certification;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipInfo;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipsResponse;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingResponseData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.UserInfo;
import it.pagopa.selfcare.onboarding.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.onboarding.connector.rest.model.InstitutionUpdate;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnBoardingInfo;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnboardingContract;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnboardingInstitutionRequest;
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

    protected static final String REQUIRED_INSTITUTION_ID_MESSAGE = "An Institution id is required";
    protected static final String REQUIRED_PRODUCT_ID_MESSAGE = "A product Id is required";

    private final PartyProcessRestClient restClient;
    private static final BinaryOperator<InstitutionInfo> MERGE_FUNCTION =
            (inst1, inst2) -> ACTIVE.name().equals(inst1.getStatus()) ? inst1 : inst2;
    private static final Function<OnboardingResponseData, InstitutionInfo> ONBOARDING_DATA_TO_INSTITUTION_INFO_FUNCTION = onboardingData -> {
        InstitutionInfo institutionInfo = new InstitutionInfo();
        institutionInfo.setInstitutionId(onboardingData.getExternalId());
        institutionInfo.setDescription(onboardingData.getDescription());
        institutionInfo.setStatus(onboardingData.getState().toString());
        institutionInfo.setAddress(onboardingData.getAddress());
        institutionInfo.setTaxCode(onboardingData.getTaxCode());
        institutionInfo.setZipCode(onboardingData.getZipCode());
        institutionInfo.setDigitalAddress(onboardingData.getDigitalAddress());
        institutionInfo.setBilling(onboardingData.getBilling());
        institutionInfo.setOrigin(onboardingData.getOrigin());
        institutionInfo.setInstitutionType(onboardingData.getInstitutionType());
        return institutionInfo;
    };

    static final Function<RelationshipInfo, UserInfo> RELATIONSHIP_INFO_TO_USER_INFO_FUNCTION = relationshipInfo -> {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(relationshipInfo.getFrom());
        userInfo.setName(relationshipInfo.getName());
        userInfo.setSurname(relationshipInfo.getSurname());
        userInfo.setEmail(relationshipInfo.getEmail());
        userInfo.setStatus(relationshipInfo.getState().toString());
        userInfo.setCertified(Certification.isCertified(relationshipInfo.getCertification()));
        userInfo.setTaxCode(relationshipInfo.getTaxCode());
        userInfo.setRole(relationshipInfo.getRole());
        userInfo.setInstitutionId(relationshipInfo.getTo());
        return userInfo;
    };


    @Autowired
    public PartyConnectorImpl(PartyProcessRestClient restClient) {
        this.restClient = restClient;
    }


    @Override
    public void onboardingOrganization(OnboardingData onboardingData) {
        Assert.notNull(onboardingData, "Onboarding data is required");
        OnboardingInstitutionRequest onboardingInstitutionRequest = new OnboardingInstitutionRequest();
        onboardingInstitutionRequest.setInstitutionExternalId(onboardingData.getInstitutionId());
        onboardingInstitutionRequest.setBilling(onboardingData.getBillingData());
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setInstitutionType(onboardingData.getInstitutionType());
        institutionUpdate.setAddress(onboardingData.getInstitutionUpdate().getAddress());
        institutionUpdate.setDescription(onboardingData.getInstitutionUpdate().getDescription());
        institutionUpdate.setDigitalAddress(onboardingData.getInstitutionUpdate().getDigitalAddress());
        institutionUpdate.setTaxCode(onboardingData.getInstitutionUpdate().getTaxCode());
        onboardingInstitutionRequest.setInstitutionUpdate(institutionUpdate);

        onboardingInstitutionRequest.setUsers(onboardingData.getUsers().stream()
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
        onboardingInstitutionRequest.setContract(onboardingContract);

        restClient.onboardingOrganization(onboardingInstitutionRequest);
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
    public RelationshipsResponse getUserInstitutionRelationships(String externalInstitutionId, String productId) {
        log.trace("getUserInstitutionRelationships start");
        log.debug("getUserInstitutionRelationships externalInstitutionId = {}, productId = {}", externalInstitutionId, productId);
        Assert.hasText(externalInstitutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        Assert.hasText(productId, REQUIRED_PRODUCT_ID_MESSAGE);
        RelationshipsResponse institutionRelationships = restClient.getUserInstitutionRelationships(
                externalInstitutionId,
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

    @Override
    public Collection<UserInfo> getUsers(String externalInstitutionId, UserInfo.UserInfoFilter userInfoFilter) {
        log.trace("getUsers start");
        log.debug("getUsers externalInstitutionId = {}, role = {}, productId = {}, productRoles = {}, userId = {}", externalInstitutionId, userInfoFilter.getRole(), userInfoFilter.getProductId(), userInfoFilter.getProductRoles(), userInfoFilter.getUserId());
        Assert.hasText(externalInstitutionId, REQUIRED_INSTITUTION_ID_MESSAGE);

        Collection<UserInfo> userInfos = Collections.emptyList();
        RelationshipsResponse institutionRelationships = restClient.getUserInstitutionRelationships(externalInstitutionId,
                userInfoFilter.getRole().orElse(null),
                userInfoFilter.getAllowedStates().orElse(null),
                userInfoFilter.getProductId().map(Set::of).orElse(null),
                userInfoFilter.getProductRoles().orElse(null),
                userInfoFilter.getUserId().orElse(null));
        if (institutionRelationships != null) {
            userInfos = institutionRelationships.stream()
                    .collect(Collectors.toMap(RelationshipInfo::getFrom,
                            RELATIONSHIP_INFO_TO_USER_INFO_FUNCTION, (userInfo1, userInfo2) -> {
                                if (userInfo1.getStatus().equals(userInfo2.getStatus())) {
                                    if (userInfo1.getRole().compareTo(userInfo2.getRole()) > 0) {
                                        userInfo1.setRole(userInfo2.getRole());
                                    }
                                } else {
                                    if ("ACTIVE".equals(userInfo2.getStatus())) {
                                        userInfo1.setRole(userInfo2.getRole());
                                        userInfo1.setStatus(userInfo2.getStatus());
                                    }
                                }
                                return userInfo1;
                            })).values();
        }
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUsers result = {}", userInfos);
        log.trace("getUsers end");
        return userInfos;
    }

    @Override
    public Institution getInstitutionByExternalId(String externalInstitutionId) {
        log.trace("getInstitution start");
        log.debug("getInstitution externalInstitutionId = {}", externalInstitutionId);
        Assert.hasText(externalInstitutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        Institution result = restClient.getInstitutionByExternalId(externalInstitutionId);
        log.debug("getInstitution result = {}", result);
        log.trace("getInstitution end");
        return result;
    }

    @Override
    public InstitutionInfo getOnboardedInstitution(String externalInstitutionId) {
        log.trace("getOnBoardedInstitution start");
        log.debug("getOnBoardedInstitution externalInstitutionId = {}", externalInstitutionId);
        Assert.hasText(externalInstitutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        OnBoardingInfo onBoardingInfo = restClient.getOnBoardingInfo(externalInstitutionId, EnumSet.of(ACTIVE));
        InstitutionInfo result = parseOnBoardingInfo(onBoardingInfo).stream()
                .findAny().orElse(null);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getOnBoardedInstitution result = {}", result);
        log.trace("getOnBoardedInstitution end");
        return result;
    }
}
