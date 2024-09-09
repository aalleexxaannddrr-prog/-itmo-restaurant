package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorsEditProfileDto {
    private String firstname;
    private String lastname;
    private String dateOfBirth;
    private String photo;

    // Геттеры и сеттеры для каждого поля
}
