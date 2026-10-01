package com.example.fuelstation.service;

import com.example.fuelstation.dto.ConsumeFuelRequest;
import com.example.fuelstation.entity.FuelStock;
import com.example.fuelstation.exception.ResourceNotFoundException;
import com.example.fuelstation.repository.FuelStockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FuelStockTransactionService {

    private final FuelStockRepository fuelStockRepository;

    public FuelStockTransactionService(FuelStockRepository fuelStockRepository) {
        this.fuelStockRepository = fuelStockRepository;
    }

    @Transactional
    public FuelStock minusFuelOnce(
            Long stationId,
            Long fuelTypeId,
            int quantity
    ) {

        FuelStock fuelStock =
                fuelStockRepository
                        .findByStationIdAndFuelTypeId(stationId, fuelTypeId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Not found ConsumeFuel"
                                )
                        );

        if (fuelStock.getQuantity() >= quantity) {
            int nowQuantity = fuelStock.getQuantity() - quantity;

            fuelStock.setQuantity(nowQuantity);

            fuelStockRepository.save(fuelStock);

            return fuelStock;
        } else {
            throw new IllegalArgumentException("400 NOT FOUND : ТОПЛИВА НЕДОСТАТОЧНО");
        }

    }
}