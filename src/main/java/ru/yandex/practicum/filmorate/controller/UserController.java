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
import java.util.Map;
import java.util.HashMap;

import ru.yandex.practicum.filmorate.dto.UserMapper;
import ru.yandex.practicum.filmorate.dto.UserRequest;
import ru.yandex.practicum.filmorate.model.User;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private long id = 1;

    @GetMapping
    public List<User> getUsers() {
        log.info("Выведение списка пользователей");
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User createUser(@Valid @RequestBody UserRequest userRequest) {
        User user = UserMapper.mapUserFromDto(userRequest);

        log.info("Начало добавления пользователя");
        long localId = generateId();
        User localUser = user.toBuilder().id(localId).build();

        log.debug(user.toString());

        users.put(localId, localUser);
        return localUser;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody UserRequest userRequest) {
        User user = UserMapper.mapUserFromDto(userRequest);

        if (!users.containsKey(user.getId())) {
            log.info("пользователя с {} id не существует", user.getId().toString());
            throw new IllegalStateException("Пользователя с данным id не существует");
        }

        log.info("Начало обновления пользователя");
        log.debug(user.toString());

        users.put(user.getId(), user);
        return user;
    }

    private long generateId() {
        return id++;
    }
}
