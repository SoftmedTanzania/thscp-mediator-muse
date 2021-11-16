package tz.go.moh.him.thscp.mediator.muse.domain;

public class FinanceBusResponse {
    private  ResponseData responseData;
    private  String signature;

    public FinanceBusResponse(ResponseData responseData, String signature) {
        this.responseData = responseData;
        this.signature = signature;
    }

    public ResponseData getResponseData() {
        return responseData;
    }

    public void setResponseData(ResponseData responseData) {
        this.responseData = responseData;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public static class ResponseData {
        int code;
        boolean success;
        String message;
        Object data;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }
}
