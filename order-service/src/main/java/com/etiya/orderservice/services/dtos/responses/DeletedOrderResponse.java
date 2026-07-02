package com.etiya.orderservice.services.dtos.responses;

public class DeletedOrderResponse {

    private int id;
    private int customerId;

    public DeletedOrderResponse() {
    }

    public DeletedOrderResponse(int id, int customerId) {
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
