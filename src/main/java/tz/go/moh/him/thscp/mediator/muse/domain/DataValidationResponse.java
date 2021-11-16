package tz.go.moh.him.thscp.mediator.muse.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DataValidationResponse {
    @SerializedName("response")
    @JsonProperty("response")
    List<DataValidationResultDetail> dataValidationResultDetails;

    public DataValidationResponse(List<DataValidationResultDetail> dataValidationResultDetails) {
        this.dataValidationResultDetails = dataValidationResultDetails;
    }

    public static class DataValidationResultDetail {
        @SerializedName("uuid")
        @JsonProperty("uuid")
        String uuid;

        @SerializedName("message")
        @JsonProperty("message")
        String message;

        public DataValidationResultDetail(String uuid, String message) {
            this.uuid = uuid;
            this.message = message;
        }

        public String getUuid() {
            return uuid;
        }

        public String getMessage() {
            return message;
        }
    }
}
