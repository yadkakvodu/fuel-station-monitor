package com.example.fuelstation;

import com.example.fuelstation.dto.ConsumeFuelRequest;
import com.example.fuelstation.entity.FuelStock;
import com.example.fuelstation.repository.FuelStockRepository;
import com.example.fuelstation.service.FuelStockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class FuelStockConcurrencyTest {

    @Autowired
    private FuelStockService fuelStockService;

    @Autowired
    private FuelStockRepository fuelStockRepository;

    @Test
    void concurrentFuelConsumption() throws InterruptedException {

        int threads = 10;

        ExecutorService executor =
                Executors.newFixedThreadPool(threads);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        CountDownLatch finishLatch =
                new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {

            executor.execute(() -> {

                try {
                    startLatch.await();
                    fuelStockService.minusFuel(1L, 1L, 100);

                } catch (Exception e) {
                    System.out.println(
                            "Ошибка: " + e.getClass().getSimpleName()
                    );

                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        finishLatch.await();
        FuelStock result = fuelStockRepository.findById(1L).orElseThrow();

        System.out.println("Final quantity: " + result.getQuantity());

        System.out.println("Final version: " + result.getVersion());

        executor.shutdown();
    }
}