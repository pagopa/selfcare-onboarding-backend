package it.pagopa.selfcare.onboarding.connector;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.onboarding.connector.api.PartyConnector;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipInfo;
import it.pagopa.selfcare.onboarding.connector.model.RelationshipsResponse;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.institutions.OnboardingResource;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.*;
import it.pagopa.selfcare.onboarding.connector.rest.client.MsCoreOnboardingApiClient;
import it.pagopa.selfcare.onboarding.connector.rest.client.MsCoreTokenApiClient;
import it.pagopa.selfcare.onboarding.connector.rest.client.MsOnboardingApiClient;
import it.pagopa.selfcare.onboarding.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.onboarding.connector.rest.mapper.InstitutionMapper;
import it.pagopa.selfcare.onboarding.connector.rest.mapper.OnboardingMapper;
import it.pagopa.selfcare.onboarding.connector.rest.model.InstitutionUpdate;
import it.pagopa.selfcare.onboarding.connector.rest.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.onboarding.connector.model.RelationshipState.ACTIVE;

@Service
@Slf4j
class PartyConnectorImpl implements PartyConnector {

    protected static final String REQUIRED_INSTITUTION_EXTERNAL_ID_MESSAGE = "An Institution external id is required";
    protected static final String REQUIRED_INSTITUTION_ID_MESSAGE = "An Institution id is required";
    protected static final String REQUIRED_PRODUCT_ID_MESSAGE = "A product Id is required";
    protected static final String REQUIRED_INSTITUTION_TAXCODE_MESSAGE = "An Institution tax code is required";

    public static final String REQUIRED_TOKEN_ID_MESSAGE = "A token Id is required";

    private final MsCoreTokenApiClient msCoreTokenApiClient;
    private final MsCoreOnboardingApiClient msCoreOnboardingApiClient;

    private final PartyProcessRestClient restClient;
    private final InstitutionMapper institutionMapper;

    private final MsOnboardingApiClient msOnboardingApiClient;

    private final OnboardingMapper onboardingMapper;

    private static final BinaryOperator<InstitutionInfo> MERGE_FUNCTION =
            (inst1, inst2) -> inst1.getUserRole().compareTo(inst2.getUserRole()) < 0 ? inst1 : inst2;
    private static final Function<OnboardingResponseData, InstitutionInfo> ONBOARDING_DATA_TO_INSTITUTION_INFO_FUNCTION = onboardingData -> {
        InstitutionInfo institutionInfo = new InstitutionInfo();
        institutionInfo.setId(onboardingData.getId());
        institutionInfo.setExternalId(onboardingData.getExternalId());
        institutionInfo.setDescription(onboardingData.getDescription());
        institutionInfo.setStatus(onboardingData.getState().toString());
        institutionInfo.setTaxCode(onboardingData.getTaxCode());
        institutionInfo.setAddress(onboardingData.getAddress());
        institutionInfo.setDigitalAddress(onboardingData.getDigitalAddress());
        institutionInfo.setZipCode(onboardingData.getZipCode());
        institutionInfo.setBilling(onboardingData.getBilling());
        institutionInfo.setOrigin(onboardingData.getOrigin());
        institutionInfo.setOriginId(onboardingData.getOriginId());
        institutionInfo.setInstitutionType(onboardingData.getInstitutionType());
        institutionInfo.setUserRole(onboardingData.getRole());
        return institutionInfo;
    };
    static final Function<RelationshipInfo, UserInfo> RELATIONSHIP_INFO_TO_USER_INFO_FUNCTION = relationshipInfo -> {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(relationshipInfo.getFrom());
        userInfo.setStatus(relationshipInfo.getState().toString());
        userInfo.setRole(relationshipInfo.getRole());
        userInfo.setInstitutionId(relationshipInfo.getTo());
        return userInfo;
    };

    @Autowired
    public PartyConnectorImpl(MsCoreTokenApiClient msCoreTokenApiClient, MsCoreOnboardingApiClient msCoreOnboardingApiClient, PartyProcessRestClient restClient, InstitutionMapper institutionMapper, MsOnboardingApiClient msOnboardingApiClient, OnboardingMapper onboardingMapper) {
        this.msCoreTokenApiClient = msCoreTokenApiClient;
        this.msCoreOnboardingApiClient = msCoreOnboardingApiClient;
        this.restClient = restClient;
        this.institutionMapper = institutionMapper;
        this.msOnboardingApiClient = msOnboardingApiClient;
        this.onboardingMapper = onboardingMapper;
    }


    @Override
    public void onboarding(OnboardingData onboardingData) {
        if (onboardingData.getInstitutionType() == InstitutionType.PA) {
            msOnboardingApiClient._onboardingPaPost(onboardingMapper.toOnboardingPaRequest(onboardingData));
        } else if (onboardingData.getInstitutionType() == InstitutionType.PSP) {
            msOnboardingApiClient._onboardingPspPost(onboardingMapper.toOnboardingPspRequest(onboardingData));
        } else {
            msOnboardingApiClient._onboardingPost(onboardingMapper.toOnboardingDefaultRequest(onboardingData));
        }
    }

    @Override
    public void onboardingOrganization(OnboardingData onboardingData) {
        Assert.notNull(onboardingData, "Onboarding data is required");
        OnboardingInstitutionRequest onboardingInstitutionRequest = new OnboardingInstitutionRequest();
        onboardingInstitutionRequest.setInstitutionExternalId(onboardingData.getInstitutionExternalId());
        onboardingInstitutionRequest.setPricingPlan(onboardingData.getPricingPlan());
        onboardingInstitutionRequest.setBilling(onboardingData.getBilling());
        onboardingInstitutionRequest.setProductId(onboardingData.getProductId());
        onboardingInstitutionRequest.setProductName(onboardingData.getProductName());
        InstitutionUpdate institutionUpdate = new InstitutionUpdate();
        institutionUpdate.setInstitutionType(onboardingData.getInstitutionType());
        institutionUpdate.setAddress(onboardingData.getInstitutionUpdate().getAddress());
        institutionUpdate.setDescription(onboardingData.getInstitutionUpdate().getDescription());
        institutionUpdate.setDigitalAddress(onboardingData.getInstitutionUpdate().getDigitalAddress());
        institutionUpdate.setTaxCode(onboardingData.getInstitutionUpdate().getTaxCode());
        institutionUpdate.setZipCode(onboardingData.getInstitutionUpdate().getZipCode());
        institutionUpdate.setPaymentServiceProvider(onboardingData.getInstitutionUpdate().getPaymentServiceProvider());
        institutionUpdate.setDataProtectionOfficer(onboardingData.getInstitutionUpdate().getDataProtectionOfficer());
        if (onboardingData.getLocation() != null) {
            institutionUpdate.setCity(onboardingData.getLocation().getCity());
            institutionUpdate.setCounty(onboardingData.getLocation().getCounty());
            institutionUpdate.setCountry(onboardingData.getLocation().getCountry());
        }
        if (Objects.nonNull(onboardingData.getInstitutionUpdate()) && Objects.nonNull(onboardingData.getInstitutionUpdate().getGeographicTaxonomies())) {
            institutionUpdate.setGeographicTaxonomyCodes(onboardingData.getInstitutionUpdate().getGeographicTaxonomies().stream()
                    .map(GeographicTaxonomy::getCode).collect(Collectors.toList()));
        }
        institutionUpdate.setRea(onboardingData.getInstitutionUpdate().getRea());
        institutionUpdate.setShareCapital(onboardingData.getInstitutionUpdate().getShareCapital());
        institutionUpdate.setBusinessRegisterPlace(onboardingData.getInstitutionUpdate().getBusinessRegisterPlace());
        institutionUpdate.setSupportEmail(onboardingData.getInstitutionUpdate().getSupportEmail());
        institutionUpdate.setSupportPhone(onboardingData.getInstitutionUpdate().getSupportPhone());
        institutionUpdate.setImported(onboardingData.getInstitutionUpdate().getImported());
        onboardingInstitutionRequest.setInstitutionUpdate(institutionUpdate);

        onboardingInstitutionRequest.setUsers(onboardingData.getUsers().stream()
                .map(userInfo -> {
                    User user = new User();
                    user.setId(userInfo.getId());
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
    public Collection<InstitutionInfo> getOnBoardedInstitutions(String productFilter) {
        log.trace("getOnBoardedInstitutions start");
        OnBoardingInfo onBoardingInfo = restClient.getOnBoardingInfo(null, EnumSet.of(ACTIVE));
        Collection<InstitutionInfo> result = parseOnBoardingInfo(onBoardingInfo, productFilter);
        log.debug("getOnBoardedInstitutions result = {}", result);
        log.trace("getOnBoardedInstitutions end");
        return result;
    }

    private Collection<InstitutionInfo> parseOnBoardingInfo(OnBoardingInfo onBoardingInfo, String productFilter) {
        log.trace("parseOnBoardingInfo start");
        log.debug("parseOnBoardingInfo onBoardingInfo = {}", onBoardingInfo);
        Collection<InstitutionInfo> institutions = Collections.emptyList();
        if (onBoardingInfo != null && onBoardingInfo.getInstitutions() != null) {
            institutions = onBoardingInfo.getInstitutions().stream()
                    .filter(institution -> productFilter == null || productFilter.isEmpty() || verifyFilter(institution, productFilter))
                    .map(ONBOARDING_DATA_TO_INSTITUTION_INFO_FUNCTION)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(InstitutionInfo::getId, Function.identity(), MERGE_FUNCTION),
                            Map::values
                    ));
        }
        log.debug("parseOnBoardingInfo result = {}", institutions);
        log.trace("parseOnBoardingInfo end");
        return institutions;
    }

    private Boolean verifyFilter(OnboardingResponseData onboardingResponseData, String productFilter) {

        if (productFilter.contains("premium")) {
            if (onboardingResponseData.getProductInfo().getId().equals(productFilter.split("-premium")[0])) {
                try {
                    restClient.verifyOnboarding(onboardingResponseData.getExternalId(), productFilter);
                    return true;
                } catch (RuntimeException e) {
                    return false;
                }
            }
        }

        return onboardingResponseData.getProductInfo().getId().equals(productFilter);
    }

    @Override
    public RelationshipsResponse getUserInstitutionRelationships(String externalInstitutionId, UserInfo.UserInfoFilter userInfoFilter) {
        log.trace("getUserInstitutionRelationships start");
        log.debug("getUserInstitutionRelationships externalInstitutionId = {}, userInfoFilter = {}", externalInstitutionId, userInfoFilter);
        Assert.hasText(externalInstitutionId, REQUIRED_INSTITUTION_EXTERNAL_ID_MESSAGE);
        Assert.notNull(userInfoFilter, "A filter is required");
        RelationshipsResponse institutionRelationships = restClient.getUserInstitutionRelationships(
                externalInstitutionId,
                userInfoFilter.getRole().orElse(null),
                userInfoFilter.getAllowedStates().orElse(null),
                userInfoFilter.getProductId().map(Set::of).orElse(null),
                userInfoFilter.getProductRoles().orElse(null),
                userInfoFilter.getUserId().orElse(null)
        );
        log.debug("getUserInstitutionRelationships institutionRelationships = {}", institutionRelationships);
        log.trace("getUserInstitutionRelationships end");
        return institutionRelationships;
    }

    @Override
    public Collection<UserInfo> getUsers(String externalInstitutionId, UserInfo.UserInfoFilter userInfoFilter) {
        log.trace("getUsers start");
        log.debug("getUsers externalInstitutionId = {}, role = {}, productId = {}, productRoles = {}, userId = {}", externalInstitutionId, userInfoFilter.getRole(), userInfoFilter.getProductId(), userInfoFilter.getProductRoles(), userInfoFilter.getUserId());
        Assert.hasText(externalInstitutionId, REQUIRED_INSTITUTION_EXTERNAL_ID_MESSAGE);

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
    public List<Institution> getInstitutionsByTaxCodeAndSubunitCode(String taxCode, String subunitCode) {
        log.trace("getInstitution start");
        log.debug("getInstitution taxCode = {}, subunitCode = {}", taxCode, subunitCode);
        Assert.hasText(taxCode, REQUIRED_INSTITUTION_TAXCODE_MESSAGE);
        InstitutionsResponse partyInstitutionResponse = restClient.getInstitutions(taxCode, subunitCode);
        List<Institution> result = partyInstitutionResponse.getInstitutions().stream()
                .map(institutionMapper::toEntity)
                .collect(Collectors.toList());
        log.debug("getInstitution result = {}", result);
        log.trace("getInstitution end");
        return result;
    }

    @Override
    public Institution getInstitutionByExternalId(String externalInstitutionId) {
        log.trace("getInstitution start");
        log.debug("getInstitution externalInstitutionId = {}", externalInstitutionId);
        Assert.hasText(externalInstitutionId, REQUIRED_INSTITUTION_EXTERNAL_ID_MESSAGE);
        InstitutionResponse partyInstitutionResponse = restClient.getInstitutionByExternalId(externalInstitutionId);
        Institution result = institutionMapper.toEntity(partyInstitutionResponse);
        log.debug("getInstitution result = {}", result);
        log.trace("getInstitution end");
        return result;
    }

    @Override
    public List<OnboardingResource> getOnboardings(String institutionId, String productId) {
        log.trace("getOnboardings start");
        log.debug("getOnboardings institutionId = {}", institutionId);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        OnboardingsResponse onboardings = restClient.getOnboardings(institutionId, productId);
        List<OnboardingResource> onboardingResources = onboardings.getOnboardings().stream()
                .map(institutionMapper::toResource)
                .collect(Collectors.toList());
        log.debug("getOnboardings result = {}", onboardingResources);
        log.trace("getOnboardings end");
        return onboardingResources;
    }

    @Override
    public Institution createInstitutionFromIpa(String taxCode, String subunitCode, String subunitType) {
        log.trace("createInstitutionFromIpa start");
        log.debug("createInstitutionFromIpa taxCode = {}, subunitCode = {}, subunitType = {}", taxCode, subunitCode, subunitType);
        Assert.hasText(taxCode, REQUIRED_INSTITUTION_TAXCODE_MESSAGE);
        InstitutionFromIpaPost institutionFromIpaPost = new InstitutionFromIpaPost();
        institutionFromIpaPost.setSubunitCode(subunitCode);
        institutionFromIpaPost.setTaxCode(taxCode);
        institutionFromIpaPost.setSubunitType(subunitType);
        InstitutionResponse partyInstitutionResponse = restClient.createInstitutionFromIpa(institutionFromIpaPost);
        Institution result = institutionMapper.toEntity(partyInstitutionResponse);
        log.debug("createInstitutionFromIpa result = {}", result);
        log.trace("createInstitutionFromIpa end");
        return result;
    }

    @Override
    public Institution createInstitutionFromANAC(OnboardingData onboardingData) {
        log.trace("createInstitutionFromAnac start");
        Assert.notNull(onboardingData, "An OnboardingData is required");
        InstitutionResponse partyInstitutionResponse = restClient.createInstitutionFromANAC(new InstitutionSeed(onboardingData));
        Institution result = institutionMapper.toEntity(partyInstitutionResponse);
        log.debug("createInstitutionFromAnac result = {}", result);
        log.trace("createInstitutionFromAnac end");
        return result;
    }

    @Override
    public Institution createInstitutionFromIVASS(OnboardingData onboardingData) {
        log.trace("createInstitutionFromIVASS start");
        Assert.notNull(onboardingData, "An OnboardingData is required");
        InstitutionResponse partyInstitutionResponse = restClient.createInstitutionFromIVASS(new InstitutionSeed(onboardingData));
        Institution result = institutionMapper.toEntity(partyInstitutionResponse);
        log.debug("createInstitutionFromIVASS result = {}", result);
        log.trace("createInstitutionFromIVASS end");
        return result;
    }

    @Override
    public Institution createInstitutionUsingExternalId(String institutionExternalId) {
        log.trace("createInstitutionUsingExternalId start");
        log.debug("createInstitutionUsingExternalId externalId = {}", institutionExternalId);
        Assert.hasText(institutionExternalId, REQUIRED_INSTITUTION_EXTERNAL_ID_MESSAGE);
        InstitutionResponse partyInstitutionResponse = restClient.createInstitutionUsingExternalId(institutionExternalId);
        Institution result = institutionMapper.toEntity(partyInstitutionResponse);
        log.debug("createInstitutionUsingExternalId result = {}", result);
        log.trace("createInstitutionUsingExternalId end");
        return result;
    }

    @Override
    public Institution createInstitutionFromInfocamere(OnboardingData onboardingData) {
        log.trace("createInstitutionFromInfocamere start");
        Assert.notNull(onboardingData, "An OnboardingData is required");
        InstitutionResponse partyInstitutionResponse = restClient.createInstitutionFromInfocamere(new InstitutionSeed(onboardingData));
        Institution result = institutionMapper.toEntity(partyInstitutionResponse);
        log.debug("createInstitutionFromInfocamere result = {}", result);
        log.trace("createInstitutionFromInfocamere end");
        return result;
    }

    @Override
    public Institution createInstitution(OnboardingData onboardingData) {
        log.trace("createInstitution start");
        Assert.notNull(onboardingData, "An OnboardingData is required");
        InstitutionResponse partyInstitutionResponse = restClient.createInstitution(new InstitutionSeed(onboardingData));
        Institution result = institutionMapper.toEntity(partyInstitutionResponse);
        log.debug("createInstitution result = {}", result);
        log.trace("createInstitution end");
        return result;
    }

    @Override
    public UserInfo getInstitutionManager(String externalInstitutionId, String productId) {
        log.trace("getInstitutionManager start");
        log.debug("getInstitutionManager externalId = {}, productId = {}", externalInstitutionId, productId);
        Assert.hasText(externalInstitutionId, REQUIRED_INSTITUTION_EXTERNAL_ID_MESSAGE);
        Assert.hasText(productId, REQUIRED_PRODUCT_ID_MESSAGE);
        RelationshipInfo relationshipInfo = restClient.getInstitutionManager(externalInstitutionId, productId);
        UserInfo result = RELATIONSHIP_INFO_TO_USER_INFO_FUNCTION.apply(relationshipInfo);
        log.debug("getInstitutionManager result = {}", result);
        log.trace("getInstitutionManager end");
        return result;
    }

    @Override
    public InstitutionInfo getInstitutionBillingData(String externalId, String productId) {
        log.trace("getInstitutionBillingData start");
        log.debug("getInstitutionBillingData externalId = {}, productId = {}", externalId, productId);
        Assert.hasText(externalId, REQUIRED_INSTITUTION_EXTERNAL_ID_MESSAGE);
        Assert.hasText(productId, REQUIRED_PRODUCT_ID_MESSAGE);
        BillingDataResponse billingDataResponse = restClient.getInstitutionBillingData(externalId, productId);
        InstitutionInfo result = institutionMapper.toInstitutionInfo(billingDataResponse);
        log.debug("getInstitutionBillingData result = {}", result);
        log.trace("getInstitutionBillingData end");
        return result;
    }

    @Override
    public void verifyOnboarding(String externalInstitutionId, String productId) {
        log.trace("verifyOnboarding start");
        log.debug("verifyOnboarding externalInstitutionId = {}, productId = {}", externalInstitutionId, productId);
        Assert.hasText(externalInstitutionId, REQUIRED_INSTITUTION_EXTERNAL_ID_MESSAGE);
        Assert.hasText(productId, REQUIRED_PRODUCT_ID_MESSAGE);
        restClient.verifyOnboarding(externalInstitutionId, productId);
        log.trace("verifyOnboarding end");
    }


    @Override
    public void verifyOnboarding(String taxCode, String subunitCode, String productId) {
        log.trace("verifyOnboarding start");
        log.debug("verifyOnboarding taxCode = {}, subunitCode = {}, productId = {}", taxCode, subunitCode, productId);
        Assert.hasText(taxCode, REQUIRED_INSTITUTION_EXTERNAL_ID_MESSAGE);
        Assert.hasText(productId, REQUIRED_PRODUCT_ID_MESSAGE);
        restClient.verifyOnboarding(taxCode, subunitCode, productId);
        log.trace("verifyOnboarding end");
    }

    @Override
    public void tokensVerify(String tokenId) {
        log.trace("tokensVerify start");
        log.debug("tokensVerify tokenId = {}", tokenId);
        Assert.hasText(tokenId, REQUIRED_TOKEN_ID_MESSAGE);
        msCoreTokenApiClient._verifyTokenUsingPOST(tokenId);
        log.trace("verifyOnboarding end");
    }

    @Override
    public void onboardingTokenComplete(String tokenId, MultipartFile contract) {
        log.trace("onboardingTokenComplete start");
        log.debug("onboardingTokenComplete tokenId = {}", tokenId);
        Assert.hasText(tokenId, REQUIRED_TOKEN_ID_MESSAGE);
        msCoreOnboardingApiClient._completeOnboardingUsingPOST(tokenId, contract);
        log.trace("onboardingTokenComplete end");
    }

    @Override
    public void deleteTokenComplete(String tokenId) {
        log.trace("deleteTokenComplete start");
        log.debug("deleteTokenComplete tokenId = {}", tokenId);
        Assert.hasText(tokenId, REQUIRED_TOKEN_ID_MESSAGE);
        msCoreOnboardingApiClient._invalidateOnboardingUsingDELETE(tokenId);
        log.trace("deleteTokenComplete end");
    }
}
