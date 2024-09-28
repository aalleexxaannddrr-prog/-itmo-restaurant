package com.example.itmorestaurant.service.api;



import com.example.itmorestaurant.dto.request.AuthenticationRequest;
import com.example.itmorestaurant.dto.request.RegisterRequest;
import com.example.itmorestaurant.dto.response.AuthenticationResponse;

import java.text.ParseException;

public interface AuthenticationService {
    AuthenticationResponse register(RegisterRequest request) throws ParseException;

    AuthenticationResponse authenticate(AuthenticationRequest request);

    boolean activateUser(String code);
}
