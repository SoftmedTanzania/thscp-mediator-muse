package tz.go.moh.him.thscp.mediator.muse.domain;

import java.util.List;

public class HealthCommodityFundingRequest {
    private List<Indicator> data;
    private String signature;

    public List<Indicator> getData() {
        return data;
    }

    public void setData(List<Indicator> data) {
        this.data = data;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
