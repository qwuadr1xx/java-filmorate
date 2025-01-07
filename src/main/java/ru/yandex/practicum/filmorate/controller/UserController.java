package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import ru.yandex.practicum.filmorate.dto.UserRequest;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserService userService = new UserService();

    @GetMapping
    public List<User> getUsers() {
        log.info("Выведение списка пользователей");

        return new ArrayList<>(userService.getUsersService());
    }

    @PostMapping
    public User createUser(@Valid @RequestBody UserRequest userRequest) {
        log.info("Начало добавления пользователя");

        return userService.createUserService(userRequest);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody UserRequest userRequest) {
        log.info("Начало обновления пользователя");

        return userService.updateUserService(userRequest);
    }
}
