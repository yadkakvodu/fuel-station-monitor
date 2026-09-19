package com.example.fuelstation.controller;

import org.springframework.ui.Model;
import com.example.fuelstation.entity.Station;
import com.example.fuelstation.service.StationService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.fuelstation.entity.FuelStock;
import com.example.fuelstation.service.FuelStockService;
import java.util.List;

@Controller
@RequestMapping("/stations")
public class StationWebController {

    private final StationService stationService;
    private final FuelStockService fuelStockService;

    public StationWebController(StationService stationService, FuelStockService fuelStockService) {

        this.stationService = stationService;
        this.fuelStockService = fuelStockService;

    }

    @GetMapping
    public String getStations(Model model) {

        List<Station> stations =
                stationService.getAllStations();

        model.addAttribute("stations", stations);

        return "stations";
    }

    @GetMapping("/{id}")
    public String getStation(
            @PathVariable Long id,
            Model model
    ) {
        Station station = stationService.getStationById(id);

        List<FuelStock> fuelStocks =
                fuelStockService.getFuelByStation(id);

        model.addAttribute("station", station);
        model.addAttribute("fuelStocks", fuelStocks);

        return "station";
    }



}