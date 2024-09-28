package com.example.itmorestaurant.service.impl;

import com.example.itmorestaurant.entities.RestaurantTable;
import com.example.itmorestaurant.entities.User;
import com.example.itmorestaurant.repository.RestaurantTableRepository;
import com.example.itmorestaurant.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class RestaurantTableService {

    private final RestaurantTableRepository tableRepository;
    private final UserRepository userRepository;

    public RestaurantTableService(RestaurantTableRepository tableRepository, UserRepository userRepository) {
        this.tableRepository = tableRepository;
        this.userRepository = userRepository;
    }

    public List<RestaurantTable> getAvailableTables() {
        return tableRepository.findByIsAvailable(true);
    }

    public void bookTable(Long tableId, String userEmail) {
        // Получаем пользователя из Optional
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Стол не найден"));

        // Проверяем, что стол доступен для бронирования
        if (!table.isAvailable()) {
            throw new IllegalStateException("Стол уже забронирован");
        }

        // Устанавливаем пользователя и меняем статус доступности стола
        table.setUser(user);
        table.setAvailable(false);
        tableRepository.save(table);
    }

}