package com.example.fuelstation.repository;

import com.example.fuelstation.entity.FuelStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FuelStockRepository extends JpaRepository<FuelStock, Long> {

    List<FuelStock> findByStationId(Long stationId);

    Optional<FuelStock> findByStationIdAndFuelTypeId(
            Long stationId,
            Long fuelTypeId
    );

}