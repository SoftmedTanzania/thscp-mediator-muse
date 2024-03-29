package tz.go.moh.him.thscp.mediator.muse.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

/**
 * Represents the Health Commodities Funding Indicator.
 */
public class Indicator {
    @SerializedName("uuid")
    @JsonProperty("uuid")
    private String uuid;

    @SerializedName("allocatedFund")
    @JsonProperty("allocatedFund")
    private BigDecimal allocatedFund;

    @SerializedName("budgetedFund")
    @JsonProperty("budgetedFund")
    private BigDecimal budgetedFund;

    @SerializedName("financialYear")
    @JsonProperty("financialYear")
    private String financialYear;

    @SerializedName("gfsCode")
    @JsonProperty("gfsCode")
    private String gfsCode;

    @SerializedName("gfsDescription")
    @JsonProperty("gfsDescription")
    private String gfsDescription;

    @SerializedName("source")
    @JsonProperty("source")
    private String source;

    @SerializedName("institutionCode")
    @JsonProperty("institutionCode")
    private String institutionCode;

    @SerializedName("activity")
    @JsonProperty("activity")
    private String activity;

    @SerializedName("date")
    @JsonProperty("date")
    private String date;

    public String getUuid() {
        return uuid;
    }

    public String getGfsDescription() {
        return gfsDescription;
    }

    public BigDecimal getAllocatedFund() {
        return allocatedFund;
    }

    public void setAllocatedFund(BigDecimal allocatedFund) {
        this.allocatedFund = allocatedFund;
    }

    public BigDecimal getBudgetedFund() {
        return budgetedFund;
    }

    public void setBudgetedFund(BigDecimal budgetedFund) {
        this.budgetedFund = budgetedFund;
    }

    public String getFinancialYear() {
        return financialYear;
    }

    public void setFinancialYear(String financialYear) {
        this.financialYear = financialYear;
    }

    public String getGfsCode() {
        return gfsCode;
    }

    public void setGfsCode(String gfsCode) {
        this.gfsCode = gfsCode;
    }

    public void setGfsDescription(String gfsDescription) {
        this.gfsDescription = gfsDescription;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getInstitutionCode() {
        return institutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        this.institutionCode = institutionCode;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
