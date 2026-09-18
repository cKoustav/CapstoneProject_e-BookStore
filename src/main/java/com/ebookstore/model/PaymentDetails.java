package com.ebookstore.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDetails {
    private String transactionId;
    private String paymentMethod;
    private String maskedCardNumber;
    private String paymentStatus;
    private BigDecimal amountPaid;
    private LocalDateTime paymentTime;

    public PaymentDetails() {
    }

    public PaymentDetails(String transactionId, String paymentMethod, String maskedCardNumber, String paymentStatus, BigDecimal amountPaid, LocalDateTime paymentTime) {
        this.transactionId = transactionId;
        this.paymentMethod = paymentMethod;
        this.maskedCardNumber = maskedCardNumber;
        this.paymentStatus = paymentStatus;
        this.amountPaid = amountPaid;
        this.paymentTime = paymentTime;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }
}
