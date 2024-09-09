package com.example.demo.controller;

import com.example.demo.dto.request.EditProfileDto;
import com.example.demo.dto.response.AnswerGetProfile;
import com.example.demo.dto.response.GetUserResponse;
import com.example.demo.dto.response.ResponseGetProfile;
import com.example.demo.dto.response.UserDTO;
import com.example.demo.entities.FileData;
import com.example.demo.entities.User;
import com.example.demo.repository.FileDataRepository;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.impl.MailSender;
import com.example.demo.service.impl.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;

import static com.example.demo.controller.AuthController.VALID_PHONE_NUMBER_REGEX;

@Tag(name = "User", description = "Контроллер предоставляющие методы доступные пользователю с ролью user")
@RestController
@RequestMapping("/user")
@SecurityRequirements()
@RequiredArgsConstructor
public class UserController {
    private final FileDataRepository fileDataRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final StorageService storageService;
    private final UserRepository userRepository;
    private final MailSender mailSender;


    private boolean isValidPhoneNumber(String phoneNumber) {
        Matcher matcher = VALID_PHONE_NUMBER_REGEX.matcher(phoneNumber);
        return matcher.matches();
    }
    @Operation(summary = "Загрузка изображения аватарки пользователя из файловой системы", description = "Этот эндпоинт позволяет загрузить изображение аватарки пользователя из файловой системы.")
    @GetMapping("/fileSystem/{fileName}")
    public ResponseEntity<?> downloadImageFromFileSystem(@PathVariable String fileName) throws IOException {
        byte[] imageData = storageService.downloadImageFromFileSystem(fileName);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf("image/png"))
                .body(imageData);
    }

    @Operation(summary = "Получить данные пользователя", description = "Этот эндпоинт возвращает данные пользователя на основе предоставленного куки.")
    @GetMapping("/user")
    public ResponseEntity<GetUserResponse> getUser(@CookieValue("refresh-jwt-cookie") String cookie) {
        GetUserResponse response = new GetUserResponse();
        UserDTO userDTO = new UserDTO();
        List<FileData> allFileData = fileDataRepository.findAll();
        String fileDataPath = null;
        for (FileData fileData : allFileData) {
            if (fileData.getUser().getId() == refreshTokenRepository.findByToken(cookie).orElse(null).getUser().getId()) {
                fileDataPath = fileData.getName();
                break;
            }
        }

        User user = refreshTokenRepository.findByToken(cookie).orElse(null).getUser();

        if (user != null) {
            userDTO.setRole(user.getRole().toString());
            userDTO.setFirstName(user.getFirstname());
            userDTO.setLastName(user.getLastname());
            userDTO.setPhoto(fileDataPath);

            response.setStatus("success");
            response.setAnswer(userDTO);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.setStatus("error");
            response.setAnswer("User not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Получить профиль", description = "Этот эндпоинт возвращает профиль пользователя на основе предоставленного куки.")
    @GetMapping("/profile")
    public ResponseEntity<Object> getProfile(@CookieValue("refresh-jwt-cookie") String cookie) {
        ResponseGetProfile response = new ResponseGetProfile();
        AnswerGetProfile answer = new AnswerGetProfile();
        response.setStatus("success");
        response.setNotify("Профиль получен");

        List<FileData> allFileData = fileDataRepository.findAll();
        String fileDataPath = null;
        Long userId = refreshTokenRepository.findByToken(cookie).orElse(null).getUser().getId();

        for (FileData fileData : allFileData) {
            if (fileData.getUser().getId().equals(userId)) {
                fileDataPath = fileData.getName();
                break;
            }
        }

        var user = refreshTokenRepository.findByToken(cookie).orElse(null).getUser();
        answer.setPhone(user.getPhoneNumber());
        answer.setDateOfBirth(user.getDateOfBirth().toString());
        answer.setFirstName(user.getFirstname());
        answer.setLastName(user.getLastname());
        answer.setEmail(user.getEmail());
        answer.setPhoto(fileDataPath);

        response.setAnswer(answer);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

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
}
