package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

import ru.yandex.practicum.filmorate.model.User;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private long id = 1;

    @GetMapping
    public String getUsers() {
        log.info("Выведение списка пользователей");
        return users.values().toString();
    }

    @PostMapping
    public void createUser(@Valid @RequestBody User user) {
        log.info("Начало добавления пользователя");
        long localId = generateId();
        log.debug(String.valueOf(localId));
        log.debug(user.toString());
        users.put(localId, user.toBuilder().id(localId).build());
    }

    @PutMapping
    public void updateUser(@Valid @RequestBody User user) {
        if (users.containsKey(user.getId())) {
            log.info("Начало обновления пользователя");
            log.debug(user.toString());
            users.put(user.getId(), user);
        } else {
            log.info("пользователя с {} id не существует", user.getId().toString());
        }
    }

    private long generateId() {
        return id++;
    }
}
