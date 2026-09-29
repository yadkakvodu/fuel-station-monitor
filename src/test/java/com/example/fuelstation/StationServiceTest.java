package com.example.fuelstation;

import com.example.fuelstation.entity.Station;
import com.example.fuelstation.exception.ResourceNotFoundException;
import com.example.fuelstation.repository.StationRepository;
import com.example.fuelstation.service.StationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StationServiceTest {

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private StationService stationService;

    @Test
    void shouldReturnAllStations() {

        List<Station> stations = List.of(
                new Station("Лукойл", "Москва"),
                new Station("Газпром", "Москва")
        );

        when(stationRepository.findAll())
                .thenReturn(stations);

        List<Station> result =
                stationService.getAllStations();

        assertEquals(stations, result);

        verify(stationRepository).findAll();
    }


    @Test
    void shouldReturnStationById() {

        Station station =
                new Station("Лукойл", "Москва");

        when(stationRepository.findById(1L))
                .thenReturn(Optional.of(station));

        Station result =
                stationService.getStationById(1L);

        assertEquals(station, result);

        verify(stationRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenStationNotFound() {

        when(stationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> stationService.getStationById(999L)
        );

        verify(stationRepository).findById(999L);
    }

    @Test
    void multiThreading() throws InterruptedException {

        ExecutorService threadPool = Executors.newFixedThreadPool(10);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(100);

        for (int i = 0; i < 100; i++) {

            int taskNumber = i;

            threadPool.execute(() -> {

                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("Task: " + taskNumber + " | " + Thread.currentThread().getName());
                finishLatch.countDown();
            });
        }

        startLatch.countDown();
        finishLatch.await();
    }

    @Test
    void concurrentFuelConsumption() throws InterruptedException {
        int threads = 10;
        int fuelPerRequest = 100;

        ExecutorService threadPool = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            threadPool.execute(() -> {
                try {
                    startLatch.await();

                    // Здесь позже будет реальное списание топлива
                    System.out.println(
                            "Thread: " + Thread.currentThread().getName()
                    );

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        finishLatch.await();

        threadPool.shutdown();
    }
}