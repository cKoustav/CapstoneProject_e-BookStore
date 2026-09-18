package com.ebookstore.repository;

import com.ebookstore.model.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;

@Repository
public class OrderRepository extends AbstractFileRepository<Order, String> {

    public OrderRepository(@Value("${app.storage.directory:data}") String storageDir) {
        super(storageDir, "orders.json", Order.class, Order::getId);
    }

    public List<Order> findByUserId(String userId) {
        if (userId == null) return List.of();
        return findAll().stream()
                .filter(o -> userId.equals(o.getUserId()))
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .toList();
    }

    public List<Order> findAllSortedByDate() {
        return findAll().stream()
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .toList();
    }
}
