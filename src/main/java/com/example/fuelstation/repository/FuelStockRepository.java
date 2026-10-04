package com.example.fuelstation.repository;

import com.example.fuelstation.entity.FuelStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FuelStockRepository extends JpaRepository<FuelStock, Long> {

    @Query("""
            SELECT fs
            FROM FuelStock fs
            JOIN FETCH fs.station
            JOIN FETCH fs.fuelType
            WHERE fs.station.id = :stationId
            """)
    List<FuelStock> findByStationId(Long stationId);

    Optional<FuelStock> findByStationIdAndFuelTypeId(
            Long stationId,
            Long fuelTypeId
    );

}