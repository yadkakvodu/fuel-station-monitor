package com.example.fuelstation.repository;

import com.example.fuelstation.entity.FuelType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FuelTypeRepository extends JpaRepository<FuelType, Long> {
}