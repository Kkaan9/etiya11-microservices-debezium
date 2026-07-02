package com.etiya.productservice.services.concretes;

import com.etiya.productservice.entities.Product;
import com.etiya.productservice.repositories.ProductRepository;
import com.etiya.productservice.services.abstracts.ProductService;
import com.etiya.productservice.services.dtos.requests.CreateProductRequest;
import com.etiya.productservice.services.dtos.requests.UpdateProductRequest;
import com.etiya.productservice.services.dtos.responses.CreatedProductResponse;
import com.etiya.productservice.services.dtos.responses.DeletedProductResponse;
import com.etiya.productservice.services.dtos.responses.GetAllProductsResponse;
import com.etiya.productservice.services.dtos.responses.GetByIdProductResponse;
import com.etiya.productservice.services.dtos.responses.UpdatedProductResponse;
import com.etiya.productservice.services.exceptions.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business layer implementation. Maps between request/response DTOs and the entity,
 * and applies business rules before delegating to the data access layer.
 */
@Service
public class ProductManager implements ProductService {

    private final ProductRepository productRepository;

    public ProductManager(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public CreatedProductResponse add(CreateProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setUnitPrice(request.getUnitPrice());
        product.setStock(request.getStock());
        product.setDescription(request.getDescription());

        Product saved = productRepository.save(product);

        return new CreatedProductResponse(
                saved.getId(),
                saved.getName(),
                saved.getUnitPrice(),
                saved.getStock(),
                saved.getDescription());
    }

    @Override
    public UpdatedProductResponse update(UpdateProductRequest request) {
        Product product = findProductOrThrow(request.getId());
        product.setName(request.getName());
        product.setUnitPrice(request.getUnitPrice());
        product.setStock(request.getStock());
        product.setDescription(request.getDescription());

        Product saved = productRepository.save(product);

        return new UpdatedProductResponse(
                saved.getId(),
                saved.getName(),
                saved.getUnitPrice(),
                saved.getStock(),
                saved.getDescription());
    }

    @Override
    public DeletedProductResponse delete(int id) {
        Product product = findProductOrThrow(id);
        productRepository.deleteById(id);
        return new DeletedProductResponse(product.getId(), product.getName());
    }

    @Override
    public List<GetAllProductsResponse> getAll() {
        return productRepository.findAll().stream()
                .map(product -> new GetAllProductsResponse(
                        product.getId(),
                        product.getName(),
                        product.getUnitPrice(),
                        product.getStock(),
                        product.getDescription()))
                .toList();
    }

    @Override
    public GetByIdProductResponse getById(int id) {
        Product product = findProductOrThrow(id);
        return new GetByIdProductResponse(
                product.getId(),
                product.getName(),
                product.getUnitPrice(),
                product.getStock(),
                product.getDescription());
    }

    private Product findProductOrThrow(int id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Product not found with id: " + id));
    }
}
