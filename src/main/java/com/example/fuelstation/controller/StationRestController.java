package com.example.fuelstation.controller;

import com.example.fuelstation.dto.FuelStockUpdateRequest;
import com.example.fuelstation.entity.FuelStock;
import com.example.fuelstation.entity.Station;
import com.example.fuelstation.service.FuelStockService;
import com.example.fuelstation.service.StationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
public class StationRestController {

    private final StationService stationService;
    private final FuelStockService fuelStockService;

    public StationRestController(StationService stationService, FuelStockService fuelStockService) {
        this.stationService = stationService;
        this.fuelStockService = fuelStockService;
    }

    @GetMapping
    public List<Station> getAllStations() {
        return stationService.getAllStations();
    }

    @GetMapping("/{id}")
    public Station getStationById(@PathVariable Long id) {
        return stationService.getStationById(id);
    }

    @GetMapping("/search")
    public List<Station> searchStations(@RequestParam String query) {
        return stationService.searchStations(query);
    }

    @GetMapping("/{id}/fuel")
    public List<FuelStock> getFuelByStation(@PathVariable Long id) {
        return fuelStockService.getFuelByStation(id);
    }

    @PutMapping("/{stationId}/fuel/{fuelTypeId}")
    public FuelStock updateFuel(
            @PathVariable Long stationId,
            @PathVariable Long fuelTypeId,
            @RequestBody @Valid FuelStockUpdateRequest request
    ) {
        return fuelStockService.updateFuel(
                stationId,
                fuelTypeId,
                request
        );
    }

}
