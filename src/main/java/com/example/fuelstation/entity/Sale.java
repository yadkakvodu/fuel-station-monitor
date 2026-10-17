package com.example.fuelstation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "station_id")
    private Station station;

    @ManyToOne
    @JoinColumn(name = "fuel_type_id")
    private FuelType fuelType;

    @Positive
    @Column(nullable = false)
    private Integer quantity;

    @Positive
    @Column(nullable = false)
    private BigDecimal pricePerLiter;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SaleStatus status;

    private LocalDateTime createdAt;

    protected Sale() {

    }

    public Sale(Station station, FuelType fuelType,
                Integer quantity, BigDecimal pricePerLiter) {
        this.station = station;
        this.fuelType = fuelType;
        this.quantity = quantity;
        this.pricePerLiter = pricePerLiter;
        this.totalAmount = pricePerLiter.multiply(BigDecimal.valueOf(quantity));
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setPricePerLiter(BigDecimal pricePerLiter) {
        this.pricePerLiter = pricePerLiter;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setStatus(SaleStatus status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Station getStation() {
        return station;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getPricePerLiter() {
        return pricePerLiter;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public SaleStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @PrePersist
    protected void onCreate() {
        status = SaleStatus.AWAITING_PAYMENT;
        createdAt = LocalDateTime.now();
    }



}