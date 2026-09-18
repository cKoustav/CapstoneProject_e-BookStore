package com.ebookstore.controller;

import com.ebookstore.dto.ApiResponse;
import com.ebookstore.dto.CancelOrderRequest;
import com.ebookstore.dto.CreateOrderRequest;
import com.ebookstore.model.Order;
import com.ebookstore.model.User;
import com.ebookstore.service.AuthService;
import com.ebookstore.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;
    private final AuthService authService;

    public OrderController(OrderService orderService, AuthService authService) {
        this.orderService = orderService;
        this.authService = authService;
    }

    private User authenticate(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new IllegalArgumentException("Authentication required. Please login.");
        }
        Optional<User> userOpt = authService.getUserByToken(authHeader);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid or expired session. Please login again.");
        }
        return userOpt.get();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Order>> placeOrder(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CreateOrderRequest request) {
        User user = authenticate(authHeader);
        Order order = orderService.createOrder(user, request);
        return ResponseEntity.ok(ApiResponse.success(order, "Order placed successfully! Transaction ID: " + order.getPaymentDetails().getTransactionId()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getMyOrders(
            @RequestHeader("Authorization") String authHeader) {
        User user = authenticate(authHeader);
        List<Order> orders = orderService.getOrdersByUser(user.getId());
        return ResponseEntity.ok(ApiResponse.success(orders, "Orders retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getOrderDetails(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {
        User user = authenticate(authHeader);
        Order order = orderService.getOrderById(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(order, "Order details retrieved"));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Order>> cancelOrder(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id,
            @RequestBody(required = false) CancelOrderRequest request) {
        User user = authenticate(authHeader);
        Order cancelledOrder = orderService.cancelOrder(id, user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(cancelledOrder, "Order #" + id + " has been cancelled successfully. Refund will be credited."));
    }
}
