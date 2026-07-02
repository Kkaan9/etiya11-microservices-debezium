package com.etiya.productservice.services.abstracts;

import com.etiya.productservice.services.dtos.requests.CreateProductRequest;
import com.etiya.productservice.services.dtos.requests.UpdateProductRequest;
import com.etiya.productservice.services.dtos.responses.CreatedProductResponse;
import com.etiya.productservice.services.dtos.responses.DeletedProductResponse;
import com.etiya.productservice.services.dtos.responses.GetAllProductsResponse;
import com.etiya.productservice.services.dtos.responses.GetByIdProductResponse;
import com.etiya.productservice.services.dtos.responses.UpdatedProductResponse;

import java.util.List;

/**
 * Business layer contract. Controllers depend on this abstraction, never on the concrete manager.
 */
public interface ProductService {

    CreatedProductResponse add(CreateProductRequest request);

    UpdatedProductResponse update(UpdateProductRequest request);

    DeletedProductResponse delete(int id);

    List<GetAllProductsResponse> getAll();

    GetByIdProductResponse getById(int id);
}
