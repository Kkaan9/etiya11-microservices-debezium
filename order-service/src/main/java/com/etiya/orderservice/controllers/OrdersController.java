package com.etiya.orderservice.controllers;

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

import com.etiya.orderservice.services.abstracts.OrderService;
import com.etiya.orderservice.services.dtos.requests.CreateOrderRequest;
import com.etiya.orderservice.services.dtos.requests.UpdateOrderRequest;
import com.etiya.orderservice.services.dtos.responses.CreatedOrderResponse;
import com.etiya.orderservice.services.dtos.responses.DeletedOrderResponse;
import com.etiya.orderservice.services.dtos.responses.GetAllOrdersResponse;
import com.etiya.orderservice.services.dtos.responses.GetByIdOrderResponse;
import com.etiya.orderservice.services.dtos.responses.UpdatedOrderResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    private final OrderService orderService;

    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<GetAllOrdersResponse> getAll() {
        System.out.println("OrderService çalıştı.");
        return orderService.getAll();
    }

    @GetMapping("/{id}")
    public GetByIdOrderResponse getById(@PathVariable int id) {
        return orderService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedOrderResponse add(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.add(request);
    }

    @PutMapping("/{id}")
    public UpdatedOrderResponse update(@PathVariable int id, @Valid @RequestBody UpdateOrderRequest request) {
        request.setId(id);
        return orderService.update(request);
    }

    @DeleteMapping("/{id}")
    public DeletedOrderResponse delete(@PathVariable int id) {
        return orderService.delete(id);
    }
}
