package com.example.itmorestaurant.controller;

import com.example.itmorestaurant.entities.RestaurantTable;
import com.example.itmorestaurant.entities.User;
import com.example.itmorestaurant.enums.Role;
import com.example.itmorestaurant.repository.RefreshTokenRepository;
import com.example.itmorestaurant.repository.RestaurantTableRepository;
import com.example.itmorestaurant.service.impl.RestaurantTableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Tag(name = "Пользователь", description = "Контроллер предоставляющие методы доступные пользователю с ролью user")
@RestController
@RequestMapping("/user")
@SecurityRequirements()
@RequiredArgsConstructor
public class UserController {
    private final RestaurantTableService restaurantTableService;
    private final RefreshTokenRepository refreshTokenRepository;
    @Operation(summary = "Получение формы бронирования", description = "Этот эндпоинт позволяет вернуть форму для бронирования свободных столов")
    @GetMapping("/booking")
    @PreAuthorize("@customAuthorizationManager.hasUserRole(#cookie, 'USER')")
    public ModelAndView  getFreeTables(Model model, @CookieValue("refresh-jwt-cookie") String cookie) {
        List<RestaurantTable> freeTables = restaurantTableService.getAvailableTables();
        model.addAttribute("tables", freeTables);
        ModelAndView menuView = getMenuTable(model, cookie);
        model.addAllAttributes(menuView.getModel());
        return new ModelAndView("booking_form");
    }
    @Operation(summary = "Бронирование стола", description = "Этот эндпоинт позволяет забронировать стол")
    @PostMapping("/book-table")
    @PreAuthorize("@customAuthorizationManager.hasUserRole(#cookie, 'USER')")
    public ModelAndView  bookTable(Model model,@RequestParam("tableId") Long tableId, @CookieValue("refresh-jwt-cookie") String cookie) {
        // Получаем текущего пользователя
        User user = refreshTokenRepository.findByToken(cookie).orElse(null).getUser();
        String userEmail = user.getEmail();
        restaurantTableService.bookTable(tableId, userEmail);
        List<RestaurantTable> freeTables = restaurantTableService.getAvailableTables();
        model.addAttribute("tables", freeTables);
        ModelAndView menuView = getMenuTable(model, cookie);
        model.addAllAttributes(menuView.getModel());
        return new ModelAndView("booking_form");
    }

    public ModelAndView getMenuTable(Model model, @CookieValue("refresh-jwt-cookie") String cookie) {
        User user = refreshTokenRepository.findByToken(cookie).orElse(null).getUser();
        model.addAttribute("userRole", user.getRole().toString());
        return new ModelAndView("menu");
    }

    @Operation(summary = "Получить данные пользователя", description = "Этот эндпоинт возвращает данные пользователя на основе предоставленного куки.")
    @GetMapping("/user-form")
    @PreAuthorize("@customAuthorizationManager.hasUserRole(#cookie, 'USER') or @customAuthorizationManager.hasUserRole(#cookie, 'ADMIN')")
    public ModelAndView getUser(@CookieValue("refresh-jwt-cookie") String cookie, Model model) {

        User user = refreshTokenRepository.findByToken(cookie).orElse(null).getUser();

        if (user != null) {
            String userRole;
            if (user.getRole() == Role.USER) {
                userRole = "гость ресторана";
            } else {
                userRole = "администратор";
            }
            model.addAttribute("role", userRole);
            model.addAttribute("firstName", user.getFirstname());
            model.addAttribute("lastName", user.getLastname());
            model.addAttribute("email", user.getEmail());
            model.addAttribute("dateOfBirth", user.getDateOfBirth());
            ModelAndView menuView = getMenuTable(model, cookie);
            model.addAllAttributes(menuView.getModel());
        } else {
            model.addAttribute("error", "Пользователь не найден");
        }

        return new ModelAndView("about_me_form");
    }
    @Operation(summary = "Получения формы контактов", description = "Этот эндпоинт позволяет вернуть форму контактой информации ресторана ")
    @GetMapping("/contact")
    @PreAuthorize("@customAuthorizationManager.hasUserRole(#cookie, 'USER') or @customAuthorizationManager.hasUserRole(#cookie, 'ADMIN')")
    public ModelAndView showContactForm(Model model, @CookieValue("refresh-jwt-cookie") String cookie) {
        ModelAndView menuView = getMenuTable(model, cookie);
        model.addAllAttributes(menuView.getModel());
        return new ModelAndView("contact_form");

    }
    @Operation(summary = "Получение начальной формы", description = "Этот эндпоинт позволяет вернуть начальную форму")
    @GetMapping("/main")
    @PreAuthorize("@customAuthorizationManager.hasUserRole(#cookie, 'USER') or @customAuthorizationManager.hasUserRole(#cookie, 'ADMIN')")
    public ModelAndView showMainForm(Model model, @CookieValue("refresh-jwt-cookie") String cookie) {
        ModelAndView menuView = getMenuTable(model, cookie);
        model.addAllAttributes(menuView.getModel());
        return new ModelAndView("main_form");

    }

}
