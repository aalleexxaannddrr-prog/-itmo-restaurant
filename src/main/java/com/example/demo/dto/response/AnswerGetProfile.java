package com.example.demo.dto.response;

import lombok.Data;

@Data
public class AnswerGetProfile {
    private String phone;
    private String dateOfBirth;
    private String typeOfWorker;
    private String firstName;
    private String lastName;
    private String email;
    private String photo;
}