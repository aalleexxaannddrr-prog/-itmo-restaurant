package com.example.itmorestaurant.controller;

import com.example.itmorestaurant.dto.request.TableNotification;
import com.example.itmorestaurant.entities.RestaurantTable;
import com.example.itmorestaurant.entities.User;
import com.example.itmorestaurant.repository.RefreshTokenRepository;
import com.example.itmorestaurant.repository.RestaurantTableRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Tag(name = "Админ", description = "Контроллер предоставляющие методы доступные пользователю с ролью администратор")
@RestController
@RequestMapping("/admin")
@SecurityRequirements()
@RequiredArgsConstructor
public class AdminController {
    private final RefreshTokenRepository refreshTokenRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ModelAndView getMenuTable(Model model, @CookieValue("refresh-jwt-cookie") String cookie) {
        User user = refreshTokenRepository.findByToken(cookie).orElse(null).getUser();
        model.addAttribute("userRole", user.getRole().toString());
        return new ModelAndView("menu");
    }
    @Operation(summary = "Получения формы для добавления стола", description = "Этот эндпоинт позволяет вернуть форму для добавления стола")
    @GetMapping("/booking")
    @PreAuthorize("@customAuthorizationManager.hasUserRole(#cookie, 'ADMIN')")
    public ModelAndView getFormForCreateTables(Model model, @CookieValue("refresh-jwt-cookie") String cookie) {
        ModelAndView menuView = getMenuTable(model, cookie);
        model.addAllAttributes(menuView.getModel());
        return new ModelAndView("booking_form_admin");
    }
    @Operation(summary = "Добавление стола", description = "Этот эндпоинт позволяет добавить стол в систему")
    @PostMapping("/tables/add")
    @PreAuthorize("@customAuthorizationManager.hasUserRole(#cookie, 'ADMIN')")
    public ModelAndView addTable(@ModelAttribute RestaurantTable restaurantTable, Model model, @CookieValue("refresh-jwt-cookie") String cookie) {
        restaurantTable.setAvailable(true); // Устанавливаем статус доступности стола по умолчанию
        restaurantTableRepository.save(restaurantTable);
        ModelAndView menuView = getMenuTable(model, cookie);
        model.addAllAttributes(menuView.getModel());
        ObjectMapper objectMapper = new ObjectMapper();
        TableNotification tableNotification = new TableNotification(restaurantTable.getNumber(), restaurantTable.getCapacity());
        try {
            String jsonMessage = objectMapper.writeValueAsString(tableNotification);
            messagingTemplate.convertAndSend("/topic/tables", jsonMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        messagingTemplate.convertAndSend("/topic/tables", "Новый стол добавлен: Номер " + restaurantTable.getNumber() + ", Вместимость: " + restaurantTable.getCapacity());
        return new ModelAndView("booking_form_admin");
    }
}
