package com.example.demo.service.api;



import com.example.demo.dto.request.AuthenticationRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.dto.response.AuthenticationResponse;
import com.example.demo.entities.User;

import java.text.ParseException;

public interface AuthenticationService {
    AuthenticationResponse register(RegisterRequest request) throws ParseException;

    void  resendActivationCode(User user) throws ParseException;

    AuthenticationResponse authenticate(AuthenticationRequest request);

    boolean activateUser(String code);
}
