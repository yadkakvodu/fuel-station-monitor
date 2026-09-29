package com.example.fuelstation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class ConsumeFuelRequest {

    @NotNull
    @Positive
    @Min(1)
    private int quantity;


}
