package it.pagopa.selfcare.onboarding.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BillingDataDto {

    @ApiModelProperty(value = "${}")
    private String description;
    private String physicalAddress;
    private String digitalAddress;
    private String taxCode;
    private String vatNumber;

}
