package tz.go.moh.him.thscp.mediator.ffars.muse.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

/**
 * Represents the Health Commodities Funding Indicator.
 */
public class Indicator {
    @SerializedName("uuid")
    @JsonProperty("uuid")
    private String uuid;

    @SerializedName("allocatedFund")
    @JsonProperty("allocatedFund")
    private int allocatedFund;

    @SerializedName("disbursedFund")
    @JsonProperty("disbursedFund")
    private int disbursedFund;

    @SerializedName("productCode")
    @JsonProperty("productCode")
    private String productCode;

    @SerializedName("program")
    @JsonProperty("program")
    private String program;

    @SerializedName("source")
    @JsonProperty("source")
    private String source;

    @SerializedName("facilityId")
    @JsonProperty("facilityId")
    private String facilityId;

    @SerializedName("startDate")
    @JsonProperty("startDate")
    private String startDate;

    @SerializedName("endDate")
    @JsonProperty("endDate")
    private String endDate;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getAllocatedFund() {
        return allocatedFund;
    }

    public void setAllocatedFund(int allocatedFund) {
        this.allocatedFund = allocatedFund;
    }

    public int getDisbursedFund() {
        return disbursedFund;
    }

    public void setDisbursedFund(int disbursedFund) {
        this.disbursedFund = disbursedFund;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
