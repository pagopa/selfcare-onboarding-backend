package it.pagopa.selfcare.onboarding.connector;

import io.github.resilience4j.retry.annotation.Retry;
import it.pagopa.selfcare.onboarding.common.InstitutionType;
import it.pagopa.selfcare.onboarding.connector.api.OnboardingMsConnector;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.rest.client.MsOnboardingApiClient;
import it.pagopa.selfcare.onboarding.connector.rest.client.MsOnboardingSupportApiClient;
import it.pagopa.selfcare.onboarding.connector.rest.client.MsOnboardingTokenApiClient;
import it.pagopa.selfcare.onboarding.connector.rest.mapper.OnboardingMapper;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.OnboardingGet;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.OnboardingResponse;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.OnboardingStatus;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.ReasonRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class OnboardingMsConnectorImpl implements OnboardingMsConnector {

    private final MsOnboardingApiClient msOnboardingApiClient;
    private final MsOnboardingSupportApiClient msOnboardingSupportApiClient;
    private final MsOnboardingTokenApiClient msOnboardingTokenApiClient;

    private final OnboardingMapper onboardingMapper;

    public OnboardingMsConnectorImpl(MsOnboardingApiClient msOnboardingApiClient,
                                     MsOnboardingTokenApiClient msOnboardingTokenApiClient,
                                     MsOnboardingSupportApiClient msOnboardingSupportApiClient,
                                     OnboardingMapper onboardingMapper) {
        this.msOnboardingApiClient = msOnboardingApiClient;
        this.msOnboardingTokenApiClient = msOnboardingTokenApiClient;
        this.msOnboardingSupportApiClient = msOnboardingSupportApiClient;
        this.onboardingMapper = onboardingMapper;
    }

    @Override
    @Retry(name = "retryTimeout")
    public void onboarding(OnboardingData onboardingData) {
        if (onboardingData.getInstitutionType() == InstitutionType.PA) {
            msOnboardingApiClient._v1OnboardingPaPost(onboardingMapper.toOnboardingPaRequest(onboardingData));
        } else if (onboardingData.getInstitutionType() == InstitutionType.PSP) {
            msOnboardingApiClient._v1OnboardingPspPost(onboardingMapper.toOnboardingPspRequest(onboardingData));
        } else {
            msOnboardingApiClient._v1OnboardingPost(onboardingMapper.toOnboardingDefaultRequest(onboardingData));
        }
    }

    @Override
    @Retry(name = "retryTimeout")
    public void onboardingUsers(OnboardingData onboardingData) {
        msOnboardingApiClient._v1OnboardingUsersPost(onboardingMapper.toOnboardingUsersRequest(onboardingData));
    }


    @Override
    @Retry(name = "retryTimeout")
    public void onboardingCompany(OnboardingData onboardingData) {
        msOnboardingApiClient._v1OnboardingPgCompletionPost(onboardingMapper.toOnboardingPgRequest(onboardingData));
    }

    @Override
    @Retry(name = "retryTimeout")
    public void onboardingTokenComplete(String onboardingId, MultipartFile contract) {
        msOnboardingApiClient._v1OnboardingOnboardingIdCompletePut(onboardingId, contract);
    }

    @Override
    @Retry(name = "retryTimeout")
    public void onboardingUsersComplete(String onboardingId, MultipartFile contract) {
        msOnboardingApiClient._v1OnboardingOnboardingIdCompleteOnboardingUsersPut(onboardingId, contract);
    }

    @Override
    @Retry(name = "retryTimeout")
    public void onboardingPending(String onboardingId) {
        msOnboardingApiClient._v1OnboardingOnboardingIdPendingGet(onboardingId);
    }

    @Override
    @Retry(name = "retryTimeout")
    public void approveOnboarding(String onboardingId) {
        msOnboardingApiClient._v1OnboardingOnboardingIdApprovePut(onboardingId);
    }

    @Override
    @Retry(name = "retryTimeout")
    public void rejectOnboarding(String onboardingId, String reason) {
        ReasonRequest reasonForReject = new ReasonRequest();
        if(StringUtils.hasText(reason)) {
            reasonForReject.setReasonForReject(reason);
        }
        msOnboardingApiClient._v1OnboardingOnboardingIdRejectPut(onboardingId, reasonForReject);
    }

    @Override
    @Retry(name = "retryTimeout")
    public OnboardingData getOnboarding(String onboardingId) {
        OnboardingGet onboardingGet = msOnboardingApiClient._v1OnboardingOnboardingIdGet(onboardingId).getBody();
        return onboardingMapper.toOnboardingData(onboardingGet);
    }

    @Override
    @Retry(name = "retryTimeout")
    public OnboardingData getOnboardingWithUserInfo(String onboardingId) {
        OnboardingGet onboardingGet = msOnboardingApiClient._v1OnboardingOnboardingIdWithUserInfoGet(onboardingId).getBody();
        return onboardingMapper.toOnboardingData(onboardingGet);
    }

    @Override
    @Retry(name = "retryTimeout")
    public Resource getContract(String onboardingId) {
        return msOnboardingTokenApiClient._v1TokensOnboardingIdContractGet(onboardingId).getBody();
    }

    @Override
    @Retry(name = "retryTimeout")
    public void onboardingPaAggregation(OnboardingData onboardingData) {
        msOnboardingApiClient._v1OnboardingPaAggregationPost(onboardingMapper.toOnboardingPaAggregationRequest(onboardingData));
    }

    @Override
    public List<OnboardingData> getByFilters(String productId, String taxCode, String origin, String originId, String subunitCode) {
        List<OnboardingResponse> result = msOnboardingSupportApiClient._v1OnboardingInstitutionOnboardingsGet(origin, originId, OnboardingStatus.COMPLETED, subunitCode, taxCode).getBody();
        return Objects.nonNull(result) ? result.stream()
                .filter(onboardingResponse -> onboardingResponse.getProductId().equals(productId))
                .map(onboardingMapper::toOnboardingData)
                .toList() : List.of();
    }
}
