package com.etiya.orderservice.repositories;

import com.etiya.orderservice.entities.Order;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Data access layer. In-memory implementation backed by a list; swap for a JPA repository later
 * without touching the business layer.
 */
@Repository
public class OrderRepository {

    private final List<Order> orders = new ArrayList<>();
    private final AtomicInteger idGenerator = new AtomicInteger(0);

    public List<Order> findAll() {
        return new ArrayList<>(orders);
    }

    public Optional<Order> findById(int id) {
        return orders.stream()
                .filter(order -> order.getId() == id)
                .findFirst();
    }

    public boolean existsById(int id) {
        return findById(id).isPresent();
    }

    public Order save(Order order) {
        if (order.getId() == 0) {
            order.setId(idGenerator.incrementAndGet());
            orders.add(order);
            return order;
        }
        deleteById(order.getId());
        orders.add(order);
        return order;
    }

    public void deleteById(int id) {
        orders.removeIf(order -> order.getId() == id);
    }
}
