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


}