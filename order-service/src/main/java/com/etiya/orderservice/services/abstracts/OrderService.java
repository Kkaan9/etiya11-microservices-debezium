package com.etiya.orderservice.services.abstracts;

import com.etiya.orderservice.services.dtos.requests.CreateOrderRequest;
import com.etiya.orderservice.services.dtos.requests.UpdateOrderRequest;
import com.etiya.orderservice.services.dtos.responses.CreatedOrderResponse;
import com.etiya.orderservice.services.dtos.responses.DeletedOrderResponse;
import com.etiya.orderservice.services.dtos.responses.GetAllOrdersResponse;
import com.etiya.orderservice.services.dtos.responses.GetByIdOrderResponse;
import com.etiya.orderservice.services.dtos.responses.UpdatedOrderResponse;

import java.util.List;

/**
 * Business layer contract. Controllers depend on this abstraction, never on the concrete manager.
 */
public interface OrderService {

    CreatedOrderResponse add(CreateOrderRequest request);

    UpdatedOrderResponse update(UpdateOrderRequest request);

    DeletedOrderResponse delete(int id);

    List<GetAllOrdersResponse> getAll();

    GetByIdOrderResponse getById(int id);
}
