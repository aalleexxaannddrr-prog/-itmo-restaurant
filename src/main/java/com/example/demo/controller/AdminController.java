package com.example.demo.controller;

import com.example.demo.dto.response.GetAllUsersResponse;
import com.example.demo.dto.response.GetUsersDto;
import com.example.demo.entities.User;
import com.example.demo.enums.Role;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.impl.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Tag(name = "Admin", description = "Контроллер предоставляющие методы доступные пользователю с ролью администратор")
@RestController
@RequestMapping("/admin")
@SecurityRequirements()
@RequiredArgsConstructor
public class AdminController {
    private final UserRepository userRepository;
    private final StorageService storageService;


    @Operation(summary = "Получить всех пользователей", description = "Этот эндпоинт возвращает список всех пользователей с пагинацией.")
    @GetMapping("/allUsers")
    public ResponseEntity<GetAllUsersResponse> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        List<User> users = userRepository.findAll();

        List<GetUsersDto> userDtos = new ArrayList<>();

        for (User user : users) {
            Role role = user.getRole();
            GetUsersDto userDto = new GetUsersDto(
                    user.getFirstname() != null ? user.getFirstname() : null,
                    user.getEmail() != null ? user.getEmail() : null,
                    user.getLastname() != null ? user.getLastname() : null,
                    user.getPhoneNumber() != null ? user.getPhoneNumber() : null,
                    user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null,
                    "http://31.129.102.70:8080/user/fileSystem/" + (user.getFileData() != null && user.getFileData().getName() != null ? user.getFileData().getName() : null),
                    user.getActivationCode() == null,
                    role != null ? role.name() : null,
                    user.getId() != null ? user.getId().toString() : null, user.getBalance()
            );
            userDtos.add(userDto);
        }

        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, userDtos.size());
        List<GetUsersDto> paginatedUserDtos = userDtos.subList(startIndex, endIndex);

        GetAllUsersResponse response = new GetAllUsersResponse();
        response.setStatus("success");
        response.setNotify("Пользователи получены");
        response.setUsers(paginatedUserDtos);
        response.setOffset(page + 1);
        response.setPageNumber(page);
        response.setTotalElements(userDtos.size());
        response.setTotalPages((int) Math.ceil((double) userDtos.size() / size));
        response.setPageSize(size);
        response.setLast((page + 1) * size >= userDtos.size());
        response.setFirst(page == 0);

        return ResponseEntity.ok(response);
    }


}
