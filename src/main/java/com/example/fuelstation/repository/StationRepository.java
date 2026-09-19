package com.example.fuelstation.repository;

import com.example.fuelstation.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StationRepository extends JpaRepository<Station, Long> {


    List<Station> findByNameContainingIgnoreCase(String name);
}