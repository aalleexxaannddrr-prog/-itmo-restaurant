package com.example.demo.dto.response;


import com.example.demo.dto.request.ErrorPasswordResetRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequestResponse {
    private String status;
    private String notify;
    private String answer;
    private ErrorPasswordResetRequestDto errors;

    // Геттеры и сеттеры
}