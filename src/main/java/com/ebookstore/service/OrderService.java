package com.ebookstore.service;

import com.ebookstore.dto.CancelOrderRequest;
import com.ebookstore.dto.CreateOrderRequest;
import com.ebookstore.dto.OrderItemRequest;
import com.ebookstore.model.*;
import com.ebookstore.repository.BookRepository;
import com.ebookstore.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    public static final long CANCELLATION_WINDOW_HOURS = 48;

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final MockPaymentGatewayService paymentGatewayService;

    public OrderService(OrderRepository orderRepository,
                        BookRepository bookRepository,
                        MockPaymentGatewayService paymentGatewayService) {
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
        this.paymentGatewayService = paymentGatewayService;
    }

    public synchronized Order createOrder(User user, CreateOrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot create an order with no items");
        }

        if (request.getDeliveryAddress() == null) {
            throw new IllegalArgumentException("Delivery address is mandatory");
        }

        // Validate items, calculate totals, verify & deduct stock
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            Book book = bookRepository.findById(itemReq.getBookId())
                    .orElseThrow(() -> new IllegalArgumentException("Book not found with ID: " + itemReq.getBookId()));

            if (itemReq.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0 for book: " + book.getTitle());
            }

            if (book.getStockQuantity() < itemReq.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for book '" + book.getTitle() + "'. Available: " + book.getStockQuantity());
            }

            BigDecimal itemSubtotal = book.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(itemSubtotal);

            OrderItem orderItem = new OrderItem(
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getPrice(),
                    itemReq.getQuantity(),
                    itemSubtotal
            );
            orderItems.add(orderItem);
        }

        // Process payment with mock gateway
        PaymentDetails paymentDetails = paymentGatewayService.processPayment(request.getPayment(), totalAmount);

        // Deduct book stock
        for (OrderItem item : orderItems) {
            Book book = bookRepository.findById(item.getBookId()).get();
            book.setStockQuantity(book.getStockQuantity() - item.getQuantity());
            bookRepository.save(book);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusHours(CANCELLATION_WINDOW_HOURS);

        Order order = new Order(
                "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(),
                user.getId(),
                user.getEmail(),
                orderItems,
                totalAmount,
                request.getDeliveryAddress(),
                paymentDetails,
                OrderStatus.CONFIRMED,
                now,
                deadline
        );

        return orderRepository.save(order);
    }

    public List<Order> getOrdersByUser(String userId) {
        return orderRepository.findByUserId(userId);
    }

    public Order getOrderById(String orderId, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You are not authorized to view this order");
        }
        return order;
    }

    public synchronized Order cancelOrder(String orderId, String userId, CancelOrderRequest request) {
        Order order = getOrderById(orderId, userId);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled");
        }

        LocalDateTime now = LocalDateTime.now();
        if (order.getCancellationDeadline() != null && now.isAfter(order.getCancellationDeadline())) {
            Duration duration = Duration.between(order.getOrderDate(), now);
            long hoursElapsed = duration.toHours();
            throw new IllegalStateException(
                    "Order cannot be cancelled. The 48-hour cancellation window has expired (" + hoursElapsed + " hours have elapsed since order placement)."
            );
        }

        // Restock items
        for (OrderItem item : order.getItems()) {
            bookRepository.findById(item.getBookId()).ifPresent(book -> {
                book.setStockQuantity(book.getStockQuantity() + item.getQuantity());
                bookRepository.save(book);
            });
        }

        // Update payment status to REFUNDED
        if (order.getPaymentDetails() != null) {
            order.getPaymentDetails().setPaymentStatus("REFUNDED");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(now);
        order.setCancellationReason(request != null && request.getReason() != null ? request.getReason() : "Cancelled by user");

        return orderRepository.save(order);
    }
}
