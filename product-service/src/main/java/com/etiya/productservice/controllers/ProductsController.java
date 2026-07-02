package com.etiya.productservice.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.etiya.productservice.services.abstracts.ProductService;
import com.etiya.productservice.services.dtos.requests.CreateProductRequest;
import com.etiya.productservice.services.dtos.requests.UpdateProductRequest;
import com.etiya.productservice.services.dtos.responses.CreatedProductResponse;
import com.etiya.productservice.services.dtos.responses.DeletedProductResponse;
import com.etiya.productservice.services.dtos.responses.GetAllProductsResponse;
import com.etiya.productservice.services.dtos.responses.GetByIdProductResponse;
import com.etiya.productservice.services.dtos.responses.UpdatedProductResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductsController {

    private final ProductService productService;

    public ProductsController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<GetAllProductsResponse> getAll() {
        System.out.println("Bu servis çalıştı");
        return productService.getAll();
    }

    @GetMapping("/{id}")
    public GetByIdProductResponse getById(@PathVariable int id) {
        return productService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedProductResponse add(@Valid @RequestBody CreateProductRequest request) {
        return productService.add(request);
    }

    @PutMapping("/{id}")
    public UpdatedProductResponse update(@PathVariable int id, @Valid @RequestBody UpdateProductRequest request) {
        request.setId(id);
        return productService.update(request);
    }

    @DeleteMapping("/{id}")
    public DeletedProductResponse delete(@PathVariable int id) {
        return productService.delete(id);
    }
}
