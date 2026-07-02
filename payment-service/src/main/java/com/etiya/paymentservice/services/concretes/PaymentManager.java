package com.etiya.paymentservice.services.concretes;

import com.etiya.paymentservice.entities.Payment;
import com.etiya.paymentservice.events.PaymentCreatedEvent;
import com.etiya.paymentservice.outbox.OutboxService;
import com.etiya.paymentservice.repositories.PaymentRepository;
import com.etiya.paymentservice.services.abstracts.PaymentService;
import com.etiya.paymentservice.services.dtos.requests.CreatePaymentRequest;
import com.etiya.paymentservice.services.dtos.requests.UpdatePaymentRequest;
import com.etiya.paymentservice.services.dtos.responses.CreatedPaymentResponse;
import com.etiya.paymentservice.services.dtos.responses.DeletedPaymentResponse;
import com.etiya.paymentservice.services.dtos.responses.GetAllPaymentsResponse;
import com.etiya.paymentservice.services.dtos.responses.GetByIdPaymentResponse;
import com.etiya.paymentservice.services.dtos.responses.UpdatedPaymentResponse;
import com.etiya.paymentservice.services.exceptions.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business layer implementation. Maps between request/response DTOs and the entity,
 * and applies business rules before delegating to the data access layer.
 */
@Service
public class PaymentManager implements PaymentService {

    /** Kafka topic the Debezium outbox router publishes the PaymentCreated message to. */
    private static final String PAYMENT_CREATED_TOPIC = "payment-created";

    private final PaymentRepository paymentRepository;
    private final OutboxService outboxService;

    public PaymentManager(PaymentRepository paymentRepository, OutboxService outboxService) {
        this.paymentRepository = paymentRepository;
        this.outboxService = outboxService;
    }

    @Override
    @Transactional
    public CreatedPaymentResponse add(CreatePaymentRequest request) {
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setCustomerId(request.getCustomerId());
        payment.setAmount(request.getAmount());
        payment.setStatus(request.getStatus());

        Payment saved = paymentRepository.save(payment);

        // Transactional Outbox: queue PaymentCreated in the outbox table instead of publishing to
        // Kafka inline. Debezium (CDC) picks up the insert and forwards it.
        outboxService.record(
                "Payment",
                String.valueOf(saved.getId()),
                "PaymentCreated",
                PAYMENT_CREATED_TOPIC,
                new PaymentCreatedEvent(
                        saved.getId(),
                        saved.getOrderId(),
                        saved.getCustomerId(),
                        saved.getAmount(),
                        saved.getStatus()));

        return new CreatedPaymentResponse(
                saved.getId(),
                saved.getOrderId(),
                saved.getCustomerId(),
                saved.getAmount(),
                saved.getStatus());
    }

    @Override
    public UpdatedPaymentResponse update(UpdatePaymentRequest request) {
        Payment payment = findPaymentOrThrow(request.getId());
        payment.setOrderId(request.getOrderId());
        payment.setCustomerId(request.getCustomerId());
        payment.setAmount(request.getAmount());
        payment.setStatus(request.getStatus());

        Payment saved = paymentRepository.save(payment);

        return new UpdatedPaymentResponse(
                saved.getId(),
                saved.getOrderId(),
                saved.getCustomerId(),
                saved.getAmount(),
                saved.getStatus());
    }

    @Override
    public DeletedPaymentResponse delete(int id) {
        Payment payment = findPaymentOrThrow(id);
        paymentRepository.deleteById(id);
        return new DeletedPaymentResponse(payment.getId(), payment.getCustomerId());
    }

    @Override
    public List<GetAllPaymentsResponse> getAll() {
        return paymentRepository.findAll().stream()
                .map(payment -> new GetAllPaymentsResponse(
                        payment.getId(),
                        payment.getOrderId(),
                        payment.getCustomerId(),
                        payment.getAmount(),
                        payment.getStatus()))
                .toList();
    }

    @Override
    public GetByIdPaymentResponse getById(int id) {
        Payment payment = findPaymentOrThrow(id);
        return new GetByIdPaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getStatus());
    }

    private Payment findPaymentOrThrow(int id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Payment not found with id: " + id));
    }
}
