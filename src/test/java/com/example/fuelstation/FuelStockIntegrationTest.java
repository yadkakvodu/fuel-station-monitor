package com.example.fuelstation;


import com.example.fuelstation.entity.FuelStock;
import com.example.fuelstation.entity.FuelType;
import com.example.fuelstation.entity.Station;
import com.example.fuelstation.repository.FuelStockRepository;
import com.example.fuelstation.repository.FuelTypeRepository;
import com.example.fuelstation.repository.StationRepository;
import com.example.fuelstation.service.FuelStockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class FuelStockIntegrationTest {

    @Autowired
    private FuelStockService fuelStockService;

    @Autowired
    private FuelStockRepository fuelStockRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private FuelTypeRepository fuelTypeRepository;

    @Test
    void minusFuel_shouldUpdateDatabase() {

        Station station = new Station(
                "Test Station",
                "Test Address"
        );

        station = stationRepository.save(station);

        FuelType fuelType = new FuelType();
        fuelType.setName("AI-92");

        fuelType = fuelTypeRepository.save(fuelType);

        FuelStock fuelStock = new FuelStock();
        fuelStock.setQuantity(50);

        fuelStock.setStation(station);
        fuelStock.setFuelType(fuelType);

        fuelStock = fuelStockRepository.save(fuelStock);

        int nowQuantity = fuelStock.getQuantity();

        fuelStockService.minusFuel(
                station.getId(),
                fuelType.getId(),
                30
        );

        FuelStock updatedFuelStock =
                fuelStockRepository
                        .findByStationIdAndFuelTypeId(
                                station.getId(),
                                fuelType.getId()
                        )
                        .orElseThrow();

        assertEquals(
                nowQuantity - 30,
                updatedFuelStock.getQuantity()
        );
    }

    @Test
    void nehvatkaFuel() {

        Optional<FuelStock> optionalFuelStock =
                fuelStockRepository.findById(1L);

        FuelStock fuelStock = optionalFuelStock.get();
        int nowQuantity = fuelStock.getQuantity();

        assertThrows(IllegalArgumentException.class, () -> fuelStockService.minusFuel(1L, 1L, 100));

        FuelStock updatedFuelStock =
                fuelStockRepository.findById(1L).orElseThrow();

        assertEquals(nowQuantity, updatedFuelStock.getQuantity());

    }

    @Test
    void minusFuel_allFuel() {

        Optional<FuelStock> optionalFuelStock =
                fuelStockRepository.findById(1L);

        FuelStock fuelStock = optionalFuelStock.get();

        fuelStockService.minusFuel(1L, 1L, 50);

        FuelStock updatedFuelStock =
                fuelStockRepository.findById(1L).orElseThrow();

        assertEquals(0, updatedFuelStock.getQuantity());
    }

}