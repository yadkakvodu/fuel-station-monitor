package com.example.fuelstation.service;

import com.example.fuelstation.entity.Station;
import com.example.fuelstation.exception.ResourceNotFoundException;
import com.example.fuelstation.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StationService {

    private final StationRepository stationRepository;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    public Station getStationById(Long id) {
        return stationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Station not found: " + id
                        )
                );
    }

    public List<Station> searchStations(String query) {
        return stationRepository.findByNameContainingIgnoreCase(query);
    }
}