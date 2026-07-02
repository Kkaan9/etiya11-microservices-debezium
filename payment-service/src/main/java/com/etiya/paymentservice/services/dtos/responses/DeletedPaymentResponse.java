package com.etiya.paymentservice.services.dtos.responses;

public class DeletedPaymentResponse {

    private int id;
    private int customerId;

    public DeletedPaymentResponse() {
    }

    public DeletedPaymentResponse(int id, int customerId) {
        this.id = id;
        this.customerId = customerId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
}
