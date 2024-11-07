package it.pagopa.selfcare.onboarding.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CheckManagerResponse {
    @ApiModelProperty(value = "${swagger.user.check-manager.model.response}", required = true)
    @JsonProperty(required = true)
    private boolean response;

    public CheckManagerResponse(boolean response) {
        this.response = response;
    }
}
