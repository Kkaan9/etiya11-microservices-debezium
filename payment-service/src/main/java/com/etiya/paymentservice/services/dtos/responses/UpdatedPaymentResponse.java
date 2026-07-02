package com.etiya.paymentservice.services.dtos.responses;

import java.math.BigDecimal;

public class UpdatedPaymentResponse {

    private int id;
    private int orderId;
    private int customerId;
    private BigDecimal amount;
    private String status;

    public UpdatedPaymentResponse() {
    }

    public UpdatedPaymentResponse(int id, int orderId, int customerId, BigDecimal amount, String status) {
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
