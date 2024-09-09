package com.example.demo.service.impl;


import com.example.demo.dto.request.AuthenticationRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.dto.response.AuthenticationResponse;
import com.example.demo.entities.User;
import com.example.demo.enums.Role;
import com.example.demo.enums.TokenType;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.api.AuthenticationService;
import com.example.demo.service.api.JwtService;
import com.example.demo.service.api.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.Random;

/**
 * Реализация интерфейса AuthenticationService.
 * Обеспечивает аутентификацию пользователей и их регистрацию.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final MailSender mailSender;

    /**
     * Генерирует активационный код для пользователя.
     *
     * @return Сгенерированный активационный код.
     */
    private String generateActivationCode() {
        int length = 4;
        String digits = "0123456789";
        Random random = new Random();

        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(digits.charAt(random.nextInt(digits.length())));
        }

        return code.toString();
    }

    /**
     * Регистрирует нового пользователя.
     *
     * @param request Запрос на регистрацию.
     * @return Ответ с данными пользователя и токенами.
     * @throws ParseException В случае ошибки парсинга даты.
     */
    @Override
    public AuthenticationResponse register(RegisterRequest request) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .dateOfBirth(format.parse(request.getDateOfBirth()))
                .phoneNumber(request.getPhoneNumber())
                .build();
        String activationCode = generateActivationCode();
        user.setActivationCode(activationCode);
        if (!StringUtils.isEmpty(user.getEmail())) {
            String message = String.format(
                    "Здравствуйте, %s! \n" +
                            "Добро пожаловать в Kotitonttu. Ваш код активации: %s",
                    user.getUsername(),
                    user.getActivationCode()
            );

            mailSender.send(user.getEmail(), "Код активации Kotitonttu", message);
        }
        user = userRepository.save(user);
        var jwt = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(user.getId());

        var roles = user.getRole().getAuthorities()
                .stream()
                .map(SimpleGrantedAuthority::getAuthority)
                .toList();

        return AuthenticationResponse.builder()
                .accessToken(jwt)
                .email(user.getEmail())
                .id(user.getId())
                .refreshToken(refreshToken.getToken())
                .roles(roles)
                .tokenType(TokenType.BEARER.name())
                .build();
    }
    @Override
    public void  resendActivationCode(User user) throws ParseException {
        String activationCode = generateActivationCode();
        user.setActivationCode(activationCode);
        if (!StringUtils.isEmpty(user.getEmail())) {
            String message = String.format(
                    "Здравствуйте, %s! \n" +
                            "Добро пожаловать в Kotitonttu. Ваш код активации: %s",
                    user.getUsername(),
                    user.getActivationCode()
            );

            mailSender.send(user.getEmail(), "Код активации Kotitonttu", message);
        }
        userRepository.save(user);
    }

    /**
     * Аутентифицирует пользователя.
     *
     * @param request Запрос на аутентификацию.
     * @return Ответ с данными пользователя и токенами.
     */
    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        var user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
        var roles = user.getRole().getAuthorities()
                .stream()
                .map(SimpleGrantedAuthority::getAuthority)
                .toList();
        var jwt = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(user.getId());
        return AuthenticationResponse.builder()
                .accessToken(jwt)
                .roles(roles)
                .email(user.getEmail())
                .id(user.getId())
                .refreshToken(refreshToken.getToken())
                .tokenType(TokenType.BEARER.name())
                .build();
    }

    /**
     * Активирует пользователя по активационному коду.
     *
     * @param code Активационный код пользователя.
     * @return true, если пользователь успешно активирован, иначе false.
     */
    @Override
    public synchronized boolean activateUser(String code) {
        User userEntity = userRepository.findByActivationCode(code);
        if (userEntity == null) {
            throw new NullPointerException("Пользователь с таким кодом активации не найден ");
        }
        if (Objects.equals(code, userEntity.getActivationCode())) {
            userEntity.setActivationCode(null);
            userRepository.save(userEntity);
            return true;
        } else {
            throw new NullPointerException("Введенный код не совпадает с истинным");
        }
    }
}
