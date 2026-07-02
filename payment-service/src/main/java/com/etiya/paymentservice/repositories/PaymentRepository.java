package com.etiya.paymentservice.repositories;

import com.etiya.paymentservice.entities.Payment;
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
public class PaymentRepository {

    private final List<Payment> payments = new ArrayList<>();
    private final AtomicInteger idGenerator = new AtomicInteger(0);

    public List<Payment> findAll() {
        return new ArrayList<>(payments);
    }

    public Optional<Payment> findById(int id) {
        return payments.stream()
                .filter(payment -> payment.getId() == id)
                .findFirst();
    }

    public boolean existsById(int id) {
        return findById(id).isPresent();
    }

    public Payment save(Payment payment) {
        if (payment.getId() == 0) {
            payment.setId(idGenerator.incrementAndGet());
            payments.add(payment);
            return payment;
        }
        deleteById(payment.getId());
        payments.add(payment);
        return payment;
    }

    public void deleteById(int id) {
        payments.removeIf(payment -> payment.getId() == id);
    }
}
