package com.etiya.productservice.repositories;

import com.etiya.productservice.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA access to the products table (PostgreSQL).
 */
public interface ProductRepository extends JpaRepository<Product, Integer> {
}
