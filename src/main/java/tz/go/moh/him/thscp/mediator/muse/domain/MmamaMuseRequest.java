package tz.go.moh.him.thscp.mediator.muse.domain;

public class MmamaMuseRequest {
    private Message message;
    private String digitalSignature;

    public MmamaMuseRequest(Message message, String digitalSignature) {
        this.message = message;
        this.digitalSignature = digitalSignature;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public String getDigitalSignature() {
        return digitalSignature;
    }

    public void setDigitalSignature(String digitalSignature) {
        this.digitalSignature = digitalSignature;
    }

    private class MessageHeader {
        private String sender;
        private String receiver;
        private String msgId;
        private String messageType;
        private String paymentType;

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public String getReceiver() {
            return receiver;
        }

        public void setReceiver(String receiver) {
            this.receiver = receiver;
        }

        public String getMsgId() {
            return msgId;
        }

        public void setMsgId(String msgId) {
            this.msgId = msgId;
        }

        public String getMessageType() {
            return messageType;
        }

        public void setMessageType(String messageType) {
            this.messageType = messageType;
        }

        public String getPaymentType() {
            return paymentType;
        }

        public void setPaymentType(String paymentType) {
            this.paymentType = paymentType;
        }
    }

    private class PaymentVoutcher{
        private String ReferenceNo;
        private String ApplyDate;
        private String SubBudgetClass;
        private String PayeeCode;
        private String PayeeBIC;
        private String PaymentDesc;
        private String Narration;
        private String PayeeName;
        private String ControlNumber;
        private String PayerBankAccount;
        private String PayeeAccountName;
        private String PayeePhoneNumber;
        private String UnappliedAccount;
        private String PayeeBankAccount;
        private String PayerBankName;
        private String PayeeBankName;
        private String Amount;
        private String Currency;
        private String PayStationId;
        private String PayeeAddress;
        private String PaymentChannel;
        private String FinancialYear;

        public String getReferenceNo() {
            return ReferenceNo;
        }

        public void setReferenceNo(String referenceNo) {
            ReferenceNo = referenceNo;
        }

        public String getApplyDate() {
            return ApplyDate;
        }

        public void setApplyDate(String applyDate) {
            ApplyDate = applyDate;
        }

        public String getSubBudgetClass() {
            return SubBudgetClass;
        }

        public void setSubBudgetClass(String subBudgetClass) {
            SubBudgetClass = subBudgetClass;
        }

        public String getPayeeCode() {
            return PayeeCode;
        }

        public void setPayeeCode(String payeeCode) {
            PayeeCode = payeeCode;
        }

        public String getPayeeBIC() {
            return PayeeBIC;
        }

        public void setPayeeBIC(String payeeBIC) {
            PayeeBIC = payeeBIC;
        }

        public String getPaymentDesc() {
            return PaymentDesc;
        }

        public void setPaymentDesc(String paymentDesc) {
            PaymentDesc = paymentDesc;
        }

        public String getNarration() {
            return Narration;
        }

        public void setNarration(String narration) {
            Narration = narration;
        }

        public String getPayeeName() {
            return PayeeName;
        }

        public void setPayeeName(String payeeName) {
            PayeeName = payeeName;
        }

        public String getControlNumber() {
            return ControlNumber;
        }

        public void setControlNumber(String controlNumber) {
            ControlNumber = controlNumber;
        }

        public String getPayerBankAccount() {
            return PayerBankAccount;
        }

        public void setPayerBankAccount(String payerBankAccount) {
            PayerBankAccount = payerBankAccount;
        }

        public String getPayeeAccountName() {
            return PayeeAccountName;
        }

        public void setPayeeAccountName(String payeeAccountName) {
            PayeeAccountName = payeeAccountName;
        }

        public String getPayeePhoneNumber() {
            return PayeePhoneNumber;
        }

        public void setPayeePhoneNumber(String payeePhoneNumber) {
            PayeePhoneNumber = payeePhoneNumber;
        }

        public String getUnappliedAccount() {
            return UnappliedAccount;
        }

        public void setUnappliedAccount(String unappliedAccount) {
            UnappliedAccount = unappliedAccount;
        }

        public String getPayeeBankAccount() {
            return PayeeBankAccount;
        }

        public void setPayeeBankAccount(String payeeBankAccount) {
            PayeeBankAccount = payeeBankAccount;
        }

        public String getPayerBankName() {
            return PayerBankName;
        }

        public void setPayerBankName(String payerBankName) {
            PayerBankName = payerBankName;
        }

        public String getPayeeBankName() {
            return PayeeBankName;
        }

        public void setPayeeBankName(String payeeBankName) {
            PayeeBankName = payeeBankName;
        }

        public String getAmount() {
            return Amount;
        }

        public void setAmount(String amount) {
            Amount = amount;
        }

        public String getCurrency() {
            return Currency;
        }

        public void setCurrency(String currency) {
            Currency = currency;
        }

        public String getPayStationId() {
            return PayStationId;
        }

        public void setPayStationId(String payStationId) {
            PayStationId = payStationId;
        }

        public String getPayeeAddress() {
            return PayeeAddress;
        }

        public void setPayeeAddress(String payeeAddress) {
            PayeeAddress = payeeAddress;
        }

        public String getPaymentChannel() {
            return PaymentChannel;
        }

        public void setPaymentChannel(String paymentChannel) {
            PaymentChannel = paymentChannel;
        }

        public String getFinancialYear() {
            return FinancialYear;
        }

        public void setFinancialYear(String financialYear) {
            FinancialYear = financialYear;
        }
    }

    public static class Message {
        private MessageHeader messageHeader;
        private PaymentVoutcher paymentVoucher;

        public MessageHeader getMessageHeader() {
            return messageHeader;
        }

        public void setMessageHeader(MessageHeader messageHeader) {
            this.messageHeader = messageHeader;
        }

        public PaymentVoutcher getPaymentVoucher() {
            return paymentVoucher;
        }

        public void setPaymentVoucher(PaymentVoutcher paymentVoucher) {
            this.paymentVoucher = paymentVoucher;
        }
    }

}
