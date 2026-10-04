package com.example.fuelstation;

import com.example.fuelstation.entity.FuelStock;
import com.example.fuelstation.repository.FuelStockRepository;
import com.example.fuelstation.service.FuelStockTransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FuelStockTransactionServiceTest {

    @Mock
    private FuelStockRepository FuelStockRepository;

    @InjectMocks
    private FuelStockTransactionService fuelStockTransactionService;

    @Test
    void minusFuelStockTransactions() {

        FuelStock fuelStock = new FuelStock();
        fuelStock.setQuantity(50);

        when(FuelStockRepository.findByStationIdAndFuelTypeId(1L, 1L)).thenReturn(Optional.of(fuelStock));

        fuelStockTransactionService.minusFuelOnce(1L, 1L, 30);
        assertEquals(20, fuelStock.getQuantity());

    }

    @Test
    void minusFuel_notEnoughFuel() {
        FuelStock fuelStock = new FuelStock();

        fuelStock.setQuantity(50);
        when(FuelStockRepository.findByStationIdAndFuelTypeId(1L, 1L)).thenReturn(Optional.of(fuelStock));
        assertThrows(IllegalArgumentException.class,
                () -> fuelStockTransactionService.minusFuelOnce(1L, 1L, 100));
        verify(FuelStockRepository, never()).save(fuelStock);
    }

    @Test
    void minusFuel_allFuel() {
        FuelStock fuelStock = new FuelStock();
        fuelStock.setQuantity(50);

        when(FuelStockRepository.findByStationIdAndFuelTypeId(1L, 1L)).thenReturn(Optional.of(fuelStock));
        fuelStockTransactionService.minusFuelOnce(1L, 1L, 50);

        assertEquals(0, fuelStock.getQuantity());
        verify(FuelStockRepository).save(fuelStock);
    }

}
