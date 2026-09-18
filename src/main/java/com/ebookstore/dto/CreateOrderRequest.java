package com.ebookstore.dto;

import com.ebookstore.model.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CreateOrderRequest {
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;

    @NotNull(message = "Delivery address is required")
    @Valid
    private Address deliveryAddress;

    @NotNull(message = "Payment request is required")
    @Valid
    private MockPaymentRequest payment;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(List<OrderItemRequest> items, Address deliveryAddress, MockPaymentRequest payment) {
        this.items = items;
        this.deliveryAddress = deliveryAddress;
        this.payment = payment;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(Address deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public MockPaymentRequest getPayment() {
        return payment;
    }

    public void setPayment(MockPaymentRequest payment) {
        this.payment = payment;
    }
}
