package com.example.itmorestaurant.dto.request;

import lombok.Data;

@Data
public class TableNotification {
    private int number;
    private int capacity;
    public TableNotification(int number, int capacity) {
        this.number = number;
        this.capacity = capacity;
    }

}
