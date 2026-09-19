package com.example.fuelstation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class FuelStockUpdateRequest {

    @NotNull
    @Positive
    private BigDecimal price;

    @NotNull
    @Min(0)
    private Integer quantity;
    public FuelStockUpdateRequest() {
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}