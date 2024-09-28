package com.example.itmorestaurant.repository;

import com.example.itmorestaurant.entities.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    /**
     * Поиск доступных столов.
     *
     * @return список доступных столов
     */
    List<RestaurantTable> findByIsAvailable(boolean isAvailable);

}
