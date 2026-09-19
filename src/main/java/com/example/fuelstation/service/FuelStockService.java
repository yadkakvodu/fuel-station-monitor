package com.example.fuelstation.service;

import com.example.fuelstation.dto.FuelStockUpdateRequest;
import com.example.fuelstation.entity.FuelStock;
import com.example.fuelstation.exception.ResourceNotFoundException;
import com.example.fuelstation.repository.FuelStockRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FuelStockService {

    private final FuelStockRepository fuelStockRepository;

    public FuelStockService(FuelStockRepository fuelStockRepository) {
        this.fuelStockRepository = fuelStockRepository;
    }

    public List<FuelStock> getFuelByStation(Long stationId) {
        return fuelStockRepository.findByStationId(stationId);
    }

    public FuelStock updateFuel(
            Long stationId,
            Long fuelTypeId,
            FuelStockUpdateRequest request
    ) {
        FuelStock fuelStock = fuelStockRepository
                .findByStationIdAndFuelTypeId(stationId, fuelTypeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Fuel stock not found"
                        )
                );

        fuelStock.setPrice(request.getPrice());
        fuelStock.setQuantity(request.getQuantity());
        fuelStock.setUpdatedAt(LocalDateTime.now());

        return fuelStockRepository.save(fuelStock);
    }



}