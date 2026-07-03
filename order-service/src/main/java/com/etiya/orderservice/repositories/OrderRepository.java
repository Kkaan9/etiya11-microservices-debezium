package com.etiya.orderservice.repositories;

import com.etiya.orderservice.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA access to the orders table (MySQL).
 */
public interface OrderRepository extends JpaRepository<Order, Integer> {
}
