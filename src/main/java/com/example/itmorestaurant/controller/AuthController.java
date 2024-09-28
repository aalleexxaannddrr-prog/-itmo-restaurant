package com.example.itmorestaurant.controller;


import com.example.itmorestaurant.dto.request.AuthenticationRequest;
import com.example.itmorestaurant.dto.request.RefreshTokenRequest;
import com.example.itmorestaurant.dto.request.RegisterRequest;
import com.example.itmorestaurant.dto.response.AuthenticationResponse;
import com.example.itmorestaurant.dto.response.RefreshTokenResponse;
import com.example.itmorestaurant.entities.User;
import com.example.itmorestaurant.repository.RefreshTokenRepository;
import com.example.itmorestaurant.repository.UserRepository;
import com.example.itmorestaurant.service.api.AuthenticationService;
import com.example.itmorestaurant.service.api.JwtService;
import com.example.itmorestaurant.service.api.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.util.Optional;


@Tag(name = "Аутентификация", description = "API аутентификации. Содержит такие операции, как вход в систему, выход из системы, обновление токена и т. д.")
@Controller
@RequestMapping("/auth")
@SecurityRequirements()
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    @Operation(summary = "Получение формы регистрации", description = "Этот эндпоинт позволяет вернуть форму регистрации пользователя")
    @GetMapping("/register")
    public ModelAndView showRegistrationForm() {
        return new ModelAndView("registration_form");
    }
    @Operation(summary = "Регистрация пользователя", description = "Этот эндпоинт позволяет зарегистрировать нового пользователя.")
    @PostMapping(value = "/register")
    public ModelAndView register(@ModelAttribute RegisterRequest request) throws ParseException {
        AuthenticationResponse authenticationResponse = authenticationService.register(request);
        userRepository.findByEmail(request.getEmail()).orElse(null);
        jwtService.generateJwtCookie(authenticationResponse.getAccessToken());
        refreshTokenService.generateRefreshTokenCookie(authenticationResponse.getRefreshToken());
        return new ModelAndView("activation_form");
    }

    @Operation(summary = "Активация пользователя", description = "Этот эндпоинт позволяет активировать пользователя.")
    @PostMapping("/activate")
    public ModelAndView activateUser(String code) {
        authenticationService.activateUser(code);
        return new ModelAndView("login_form");
    }
    @Operation(summary = "Получения формы логина", description = "Этот эндпоинт позволяет вернуть форму логина")
    @GetMapping("/login")
    public ModelAndView showLoginForm() {

        return new ModelAndView("login_form");
    }

    @Operation(summary = "Вход пользователя", description = "Этот эндпоинт позволяет пользователю войти в систему.")
    @PostMapping("/login")
    public ModelAndView authenticate(@ModelAttribute AuthenticationRequest request, HttpServletResponse response,Model model) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        User user = optionalUser.get();
        AuthenticationResponse authenticationResponse = authenticationService.authenticate(request);

        // Генерация JWT и refresh-токенов в куки
        ResponseCookie jwtCookie = jwtService.generateJwtCookie(authenticationResponse.getAccessToken());
        ResponseCookie refreshTokenCookie = refreshTokenService.generateRefreshTokenCookie(authenticationResponse.getRefreshToken());
        // Установка куки в ответ
        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        logger.info("Token: {}", refreshTokenCookie.toString());
        logger.info("Token: {}", user.getRole().toString());
        model.addAttribute("userRole", user.getRole().toString());
        // Перенаправление на страницу
        return new ModelAndView("main_form");
    }


    @Operation(summary = "Обновление токена", description = "Этот эндпоинт позволяет обновить токен.")
    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(refreshTokenService.generateNewToken(request));
    }

    @Operation(summary = "Обновление токена через куки", description = "Этот эндпоинт позволяет обновить токен с использованием куки.")
    @PostMapping("/refresh-token-cookie")
    public ResponseEntity<Void> refreshTokenCookie(HttpServletRequest request) {
        String refreshToken = refreshTokenService.getRefreshTokenFromCookies(request);
        RefreshTokenResponse refreshTokenResponse = refreshTokenService
                .generateNewToken(new RefreshTokenRequest(refreshToken));
        ResponseCookie NewJwtCookie = jwtService.generateJwtCookie(refreshTokenResponse.getAccessToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, NewJwtCookie.toString())
                .build();
    }

    @Operation(summary = "Получение аутентификации", description = "Этот эндпоинт позволяет получить аутентификацию.")
    @GetMapping("/info")
    public Authentication getAuthentication(@RequestBody AuthenticationRequest request) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    }

    @Operation(summary = "Выход из системы", description = "Этот эндпоинт позволяет пользователю выйти из системы.")
    @GetMapping("/logout")
    public ModelAndView logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = refreshTokenService.getRefreshTokenFromCookies(request);
        if (refreshToken != null) {
            refreshTokenService.deleteByToken(refreshToken);
        }

        ResponseCookie jwtCookie = jwtService.getCleanJwtCookie();
        ResponseCookie refreshTokenCookie = refreshTokenService.getCleanRefreshTokenCookie();

        // Устанавливаем куки в ответ
        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        // Перенаправляем на страницу логина
        return new ModelAndView("login_form");
    }

}
