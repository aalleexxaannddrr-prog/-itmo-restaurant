package com.example.itmorestaurant.entities;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "restaurant_table")
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Номер стола.
     */
    private int number;

    /**
     * Вместимость стола.
     */
    private int capacity;

    /**
     * Статус доступности стола.
     */
    private boolean isAvailable;
    /**
     * Пользователь, забронировавший стол (связь один ко многим).
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;  // Пользователь, забронировавший стол
}
