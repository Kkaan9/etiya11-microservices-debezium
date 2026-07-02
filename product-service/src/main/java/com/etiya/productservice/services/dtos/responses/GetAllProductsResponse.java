package com.etiya.productservice.services.dtos.responses;

import java.math.BigDecimal;

public class GetAllProductsResponse {

    private int id;
    private String name;
    private BigDecimal unitPrice;
    private int stock;
    private String description;

    public GetAllProductsResponse() {
    }

    public GetAllProductsResponse(int id, String name, BigDecimal unitPrice, int stock, String description) {
        this.id = id;
        this.name = name;
        this.unitPrice = unitPrice;
        this.stock = stock;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
