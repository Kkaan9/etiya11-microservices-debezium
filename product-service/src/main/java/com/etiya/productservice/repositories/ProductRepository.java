package com.etiya.productservice.repositories;

import com.etiya.productservice.entities.Product;
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
public class ProductRepository {

    private final List<Product> products = new ArrayList<>();
    private final AtomicInteger idGenerator = new AtomicInteger(0);

    public List<Product> findAll() {
        return new ArrayList<>(products);
    }

    public Optional<Product> findById(int id) {
        return products.stream()
                .filter(product -> product.getId() == id)
                .findFirst();
    }

    public boolean existsById(int id) {
        return findById(id).isPresent();
    }

    public Product save(Product product) {
        if (product.getId() == 0) {
            product.setId(idGenerator.incrementAndGet());
            products.add(product);
            return product;
        }
        deleteById(product.getId());
        products.add(product);
        return product;
    }

    public void deleteById(int id) {
        products.removeIf(product -> product.getId() == id);
    }
}
