package com.ebookstore.service;

import com.ebookstore.dto.MockPaymentRequest;
import com.ebookstore.model.PaymentDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MockPaymentGatewayService {

    public PaymentDetails processPayment(MockPaymentRequest request, BigDecimal amount) {
        if (request.isSimulateFailure()) {
            throw new IllegalArgumentException("Payment was declined by the bank: Simulated payment failure.");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid payment amount: " + amount);
        }

        String method = request.getPaymentMethod() != null ? request.getPaymentMethod().toUpperCase() : "CARD";
        String masked = maskPaymentIdentifier(request);

        // Generate mock transaction ID
        String txnId = "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return new PaymentDetails(
                txnId,
                method,
                masked,
                "SUCCESS",
                amount,
                LocalDateTime.now()
        );
    }

    private String maskPaymentIdentifier(MockPaymentRequest request) {
        if (request.getCardNumber() != null && !request.getCardNumber().isBlank()) {
            String num = request.getCardNumber().replaceAll("\\s+", "");
            if (num.length() >= 4) {
                return "XXXX-XXXX-XXXX-" + num.substring(num.length() - 4);
            }
            return "XXXX-CARD";
        }
        if (request.getUpiId() != null && !request.getUpiId().isBlank()) {
            String upi = request.getUpiId();
            int atIndex = upi.indexOf('@');
            if (atIndex > 2) {
                return upi.substring(0, 2) + "***" + upi.substring(atIndex);
            }
            return upi;
        }
        return "N/A";
    }
}
