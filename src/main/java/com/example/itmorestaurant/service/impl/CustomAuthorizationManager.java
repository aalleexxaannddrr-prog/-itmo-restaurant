package com.example.itmorestaurant.service.impl;

import com.example.itmorestaurant.repository.RefreshTokenRepository;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthorizationManager {
    private final RefreshTokenRepository refreshTokenRepository;

    public CustomAuthorizationManager(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public boolean hasUserRole(String cookie, String role) {
        return refreshTokenRepository.findByToken(cookie)
                .map(token -> token.getUser().getRole().toString().equals(role))
                .orElse(false);
    }

}
