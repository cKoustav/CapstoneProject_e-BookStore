package com.ebookstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private String id;
    private String userId;
    private String userEmail;
    private List<OrderItem> items = new ArrayList<>();
    private BigDecimal totalAmount;
    private Address deliveryAddress;
    private PaymentDetails paymentDetails;
    private OrderStatus status;
    private LocalDateTime orderDate;
    private LocalDateTime cancellationDeadline;
    private LocalDateTime cancelledAt;
    private String cancellationReason;

    public Order() {
    }

    public Order(String id, String userId, String userEmail, List<OrderItem> items, BigDecimal totalAmount,
                 Address deliveryAddress, PaymentDetails paymentDetails, OrderStatus status,
                 LocalDateTime orderDate, LocalDateTime cancellationDeadline) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.items = items;
        this.totalAmount = totalAmount;
        this.deliveryAddress = deliveryAddress;
        this.paymentDetails = paymentDetails;
        this.status = status;
        this.orderDate = orderDate;
        this.cancellationDeadline = cancellationDeadline;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(Address deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public PaymentDetails getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(PaymentDetails paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDateTime getCancellationDeadline() {
        return cancellationDeadline;
    }

    public void setCancellationDeadline(LocalDateTime cancellationDeadline) {
        this.cancellationDeadline = cancellationDeadline;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    @JsonIgnore
    public boolean isCancellable() {
        if (this.status != OrderStatus.CONFIRMED && this.status != OrderStatus.PROCESSING) {
            return false;
        }
        if (this.cancellationDeadline == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(this.cancellationDeadline);
    }
}
