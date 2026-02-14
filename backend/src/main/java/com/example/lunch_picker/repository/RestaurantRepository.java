package com.example.lunch_picker.repository;

import com.example.lunch_picker.model.RestaurantChoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantRepository
        extends JpaRepository<RestaurantChoice, Long> {
    List<RestaurantChoice> findBySessionIdOrderByIdAsc(String sessionId);
    boolean existsBySessionIdAndRestaurantIgnoreCase(String sessionId, String restaurant);
}
