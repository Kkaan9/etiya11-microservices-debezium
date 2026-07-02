package com.etiya.paymentservice.services.abstracts;

import com.etiya.paymentservice.services.dtos.requests.CreatePaymentRequest;
import com.etiya.paymentservice.services.dtos.requests.UpdatePaymentRequest;
import com.etiya.paymentservice.services.dtos.responses.CreatedPaymentResponse;
import com.etiya.paymentservice.services.dtos.responses.DeletedPaymentResponse;
import com.etiya.paymentservice.services.dtos.responses.GetAllPaymentsResponse;
import com.etiya.paymentservice.services.dtos.responses.GetByIdPaymentResponse;
import com.etiya.paymentservice.services.dtos.responses.UpdatedPaymentResponse;

import java.util.List;

/**
 * Business layer contract. Controllers depend on this abstraction, never on the concrete manager.
 */
public interface PaymentService {

    CreatedPaymentResponse add(CreatePaymentRequest request);

    UpdatedPaymentResponse update(UpdatePaymentRequest request);

    DeletedPaymentResponse delete(int id);

    List<GetAllPaymentsResponse> getAll();

    GetByIdPaymentResponse getById(int id);
}
