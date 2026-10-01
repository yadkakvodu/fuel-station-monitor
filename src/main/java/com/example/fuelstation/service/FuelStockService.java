package com.example.fuelstation.service;

import com.example.fuelstation.dto.ConsumeFuelRequest;
import com.example.fuelstation.dto.FuelStockUpdateRequest;
import com.example.fuelstation.entity.FuelStock;
import com.example.fuelstation.exception.ResourceNotFoundException;
import com.example.fuelstation.repository.FuelStockRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FuelStockService {

    private final FuelStockRepository fuelStockRepository;
    private final FuelStockTransactionService fuelStockTransactionService;

    public FuelStockService(FuelStockRepository fuelStockRepository, FuelStockTransactionService fuelStockTransactionService) {
        this.fuelStockRepository = fuelStockRepository;
        this.fuelStockTransactionService = fuelStockTransactionService;
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

    public FuelStock minusFuel(
            Long stationId,
            Long fuelTypeId,
            int quantity
    ) {
        int maxAttempts = 10;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return fuelStockTransactionService.minusFuelOnce(
                        stationId,
                        fuelTypeId,
                        quantity
                );
            } catch (ObjectOptimisticLockingFailureException e) {

                if (attempt == maxAttempts) {
                    throw e;
                }
            }
        }

        throw new IllegalStateException("Unexpected error");
    }


}