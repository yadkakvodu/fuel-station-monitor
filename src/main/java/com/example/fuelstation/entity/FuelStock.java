package com.example.fuelstation.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fuel_stock")
public class FuelStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "station_id")
    private Station station;

    @ManyToOne
    @JoinColumn(name = "fuel_type_id")
    private FuelType fuelType;

    private BigDecimal price;

    private Integer quantity;

    private LocalDateTime updatedAt;

    public FuelStock() {
    }

    public FuelStock(
            Station station,
            FuelType fuelType,
            BigDecimal price,
            Integer quantity,
            LocalDateTime updatedAt
    ) {
        this.station = station;
        this.fuelType = fuelType;
        this.price = price;
        this.quantity = quantity;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}