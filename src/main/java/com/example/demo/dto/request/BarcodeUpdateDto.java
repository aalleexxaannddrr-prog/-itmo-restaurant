package com.example.demo.dto.request;

import lombok.Data;

@Data
public class BarcodeUpdateDto {
    private Long code;
    private boolean used;

    // Конструктор, геттеры, сеттеры
    // ...
}
