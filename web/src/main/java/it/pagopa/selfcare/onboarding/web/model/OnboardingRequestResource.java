package it.pagopa.selfcare.onboarding.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

@Data
public class OnboardingRequestResource {


    @ApiModelProperty(value = "${swagger.onboarding.model.status}", required = true)
    @JsonProperty(required = true)
    private String status;

    @ApiModelProperty(value = "${swagger.onboarding.model.institutionInfo}", required = true)
    @JsonProperty(required = true)
    private InstitutionInfo institutionInfo;

    @ApiModelProperty(value = "${swagger.onboarding.model.manager}", required = true)
    @JsonProperty(required = true)
    private UserInfo manager;

    @ApiModelProperty(value = "${swagger.onboarding.model.admins}")
    private List<UserInfo> admins;

    @ApiModelProperty(value = "${swagger.dashboard.onboarding-request.model.productId}")
    private String productId;

    @Data
    @EqualsAndHashCode(of = "id")
    public static class InstitutionInfo {

        @ApiModelProperty(value = "${swagger.onboarding.institutions.model.id}", required = true)
        @JsonProperty(required = true)
        private String id;

        @ApiModelProperty(value = "${swagger.onboarding.institutions.model.name}", required = true)
        @JsonProperty(required = true)
        private String name;

        @ApiModelProperty(value = "${swagger.onboarding.institutions.model.institutionType}", required = true)
        @JsonProperty(required = true)
        private InstitutionType institutionType;

        @ApiModelProperty(value = "${swagger.onboarding.institutions.model.address}", required = true)
        @JsonProperty(required = true)
        private String address;

        @ApiModelProperty(value = "${swagger.onboarding.institutions.model.zipCode}", required = true)
        @JsonProperty(required = true)
        private String zipCode;

        @ApiModelProperty(value = "${swagger.onboarding.institutions.model.city}")
        private String city;

        @ApiModelProperty(value = "${swagger.onboarding.institutions.model.country}")
        private String country;

        @ApiModelProperty(value = "${swagger.onboarding.institutions.model.county}")
        private String county;

        @ApiModelProperty(value = "${swagger.onboarding.institutions.model.digitalAddress}", required = true)
        @JsonProperty(required = true)
        private String mailAddress;

        @ApiModelProperty(value = "${swagger.onboarding.institutions.model.taxCode}", required = true)
        @JsonProperty(required = true)
        private String fiscalCode;

        @ApiModelProperty(value = "${swagger.onboarding.institutions.model.vatNumber}", required = true)
        @JsonProperty(required = true)
        private String vatNumber;

        @ApiModelProperty(value = "${swagger.onboarding.institutions.model.recipientCode}", required = true)
        @JsonProperty(required = true)
        private String recipientCode;

        @ApiModelProperty(value = "${swagger.onboarding.institutions.model.pspData}")
        private PspData pspData;

        @ApiModelProperty(value = "${swagger.onboarding.institutions.model.dpoData}")
        private DpoData dpoData;

        @Data
        public static class PspData {

            @ApiModelProperty(value = "${swagger.onboarding.institutions.model.pspData.businessRegisterNumber}", required = true)
            @JsonProperty(required = true)
            private String businessRegisterNumber;

            @ApiModelProperty(value = "${swagger.onboarding.institutions.model.pspData.legalRegisterName}", required = true)
            @JsonProperty(required = true)
            private String legalRegisterName;

            @ApiModelProperty(value = "${swagger.onboarding.institutions.model.pspData.legalRegisterNumber}", required = true)
            @JsonProperty(required = true)
            private String legalRegisterNumber;

            @ApiModelProperty(value = "${swagger.onboarding.institutions.model.pspData.abiCode}", required = true)
            @JsonProperty(required = true)
            private String abiCode;

            @ApiModelProperty(value = "${swagger.onboarding.institutions.model.pspData.vatNumberGroup}", required = true)
            @JsonProperty(required = true)
            private Boolean vatNumberGroup;
        }


        @Data
        public static class DpoData {

            @ApiModelProperty(value = "${swagger.onboarding.institutions.model.dpoData.address}", required = true)
            @JsonProperty(required = true)
            @NotBlank
            private String address;

            @ApiModelProperty(value = "${swagger.onboarding.institutions.model.dpoData.pec}", required = true)
            @JsonProperty(required = true)
            @NotBlank
            @Email
            private String pec;

            @ApiModelProperty(value = "${swagger.onboarding.institutions.model.dpoData.email}", required = true)
            @JsonProperty(required = true)
            @NotBlank
            @Email
            private String email;
        }
    }

    @Data
    public static class UserInfo {

        @ApiModelProperty(value = "${swagger.onboarding.user.model.id}", required = true)
        @JsonProperty(required = true)
        private UUID id;

        @ApiModelProperty(value = "${swagger.onboarding.user.model.name}", required = true)
        @JsonProperty(required = true)
        private String name;

        @ApiModelProperty(value = "${swagger.onboarding.user.model.surname}", required = true)
        @JsonProperty(required = true)
        private String surname;

        @ApiModelProperty(value = "${swagger.onboarding.user.model.institutionalEmail}", required = true)
        @JsonProperty(required = true)
        private String email;

        @ApiModelProperty(value = "${swagger.onboarding.user.model.fiscalCode}", required = true)
        @JsonProperty(required = true)
        private String fiscalCode;

    }
}
