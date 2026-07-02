package com.etiya.paymentservice.controllers;

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

import com.etiya.paymentservice.services.abstracts.PaymentService;
import com.etiya.paymentservice.services.dtos.requests.CreatePaymentRequest;
import com.etiya.paymentservice.services.dtos.requests.UpdatePaymentRequest;
import com.etiya.paymentservice.services.dtos.responses.CreatedPaymentResponse;
import com.etiya.paymentservice.services.dtos.responses.DeletedPaymentResponse;
import com.etiya.paymentservice.services.dtos.responses.GetAllPaymentsResponse;
import com.etiya.paymentservice.services.dtos.responses.GetByIdPaymentResponse;
import com.etiya.paymentservice.services.dtos.responses.UpdatedPaymentResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentsController {

    private final PaymentService paymentService;

    public PaymentsController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public List<GetAllPaymentsResponse> getAll() {
        return paymentService.getAll();
    }

    @GetMapping("/{id}")
    public GetByIdPaymentResponse getById(@PathVariable int id) {
        return paymentService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedPaymentResponse add(@Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.add(request);
    }

    @PutMapping("/{id}")
    public UpdatedPaymentResponse update(@PathVariable int id, @Valid @RequestBody UpdatePaymentRequest request) {
        request.setId(id);
        return paymentService.update(request);
    }

    @DeleteMapping("/{id}")
    public DeletedPaymentResponse delete(@PathVariable int id) {
        return paymentService.delete(id);
    }
}
