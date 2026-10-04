package com.example.fuelstation;

import com.example.fuelstation.repository.FuelStockRepository;
import com.example.fuelstation.service.FuelStockService;
import com.example.fuelstation.service.FuelStockTransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.fuelstation.entity.FuelStock;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuelStockServiceTest {


    @Mock
    private FuelStockRepository fuelStockRepository;

    @Mock
    private FuelStockTransactionService fuelStockTransactionService;

    @InjectMocks
    private FuelStockService fuelStockService;


    @Test
    void minusFuel_retryAfterOptimisticLock() {

        FuelStock fuelStock = new FuelStock();
        fuelStock.setQuantity(50);

        when(fuelStockTransactionService.minusFuelOnce(1L, 1L, 30))
                .thenThrow(ObjectOptimisticLockingFailureException.class)
                .thenReturn(fuelStock);

        fuelStockService.minusFuel(1L, 1L, 30);

        verify(fuelStockTransactionService, times(2))
                .minusFuelOnce(1L, 1L, 30);

    }

}