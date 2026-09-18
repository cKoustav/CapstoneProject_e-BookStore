package com.ebookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class MockPaymentRequest {
    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // e.g., CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING

    private String cardHolderName;

    @Pattern(regexp = "^[0-9]{13,19}$|^$", message = "Card number must be 13-19 digits")
    private String cardNumber;

    private String expiryDate; // MM/YY

    private String cvv;

    private String upiId; // For UPI payments

    private boolean simulateFailure; // For testing failed payment scenario

    public MockPaymentRequest() {
    }

    public MockPaymentRequest(String paymentMethod, String cardHolderName, String cardNumber, String expiryDate, String cvv) {
        this.paymentMethod = paymentMethod;
        this.cardHolderName = cardHolderName;
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    public boolean isSimulateFailure() {
        return simulateFailure;
    }

    public void setSimulateFailure(boolean simulateFailure) {
        this.simulateFailure = simulateFailure;
    }
}
