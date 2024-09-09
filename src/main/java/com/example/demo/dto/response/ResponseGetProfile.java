package com.example.demo.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseGetProfile {
    private String status;
    private String notify;
    private AnswerGetProfile answer;
}