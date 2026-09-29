package com.example.fuelstation;

import com.example.fuelstation.repository.FuelStockRepository;
import com.example.fuelstation.service.FuelStockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.fuelstation.entity.FuelStock;
import java.util.Optional;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FuelStockServiceTest {

    @Mock
    private FuelStockRepository fuelStockRepository;

    @InjectMocks
    private FuelStockService fuelStockService;

    @Test
    void concurrentFuelConsumption() throws InterruptedException {


    }
}