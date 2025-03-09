package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import ru.yandex.practicum.filmorate.dto.UserRequest;
import ru.yandex.practicum.filmorate.model.FeedRecord;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<User> getUsers() {
        log.info("Выведение списка пользователей");

        return new ArrayList<>(userService.getUsers());
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable final long id) {
        log.info("Выведение пользователя c id {}", id);

        return userService.getUserById(id);
    }

    @GetMapping("/{id}/friends")
    public List<User> getUserFriends(@PathVariable final long id) {
        log.info("Получение списка друзей пользователя {}", id);

        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getIntersectionFriends(@PathVariable final long id, @PathVariable final long otherId) {
        log.info("Получение пересечения друзей");

        return userService.getIntersectionFriends(id, otherId);
    }

    @GetMapping("/{id}/feed")
    public List<FeedRecord> getFeedRecord(@PathVariable final long id) {
        log.info("Получение последней записи с id: {}", id);

        return userService.getFeedRecord(id);
    }

    @PostMapping
    public User createUser(@Valid @RequestBody final UserRequest userRequest) {
        log.info("Начало добавления пользователя");

        return userService.createUser(userRequest);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody final UserRequest userRequest) {
        log.info("Начало обновления пользователя");

        return userService.updateUser(userRequest);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable final long id, @PathVariable final long friendId) {
        log.info("Добавление друга(возвращаемый объект - User с friendId {}", friendId);

        return userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User removeFriend(@PathVariable final long id, @PathVariable final long friendId) {
        log.info("Удаление друга(возвращаемый объект - User с friendId {}", friendId);

        return userService.removeFriend(id, friendId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable final long userId) {
        log.info("Удаление пользователя");

        userService.deleteUser(userId);
    }

    @GetMapping("/{id}/recommendations")
    public List<Film> getUsersRecommendations(@PathVariable final long id) {
        log.info("Получение рекомендаций фильмов для пользователя с id {}", id);
        return userService.getUsersRecommendations(id);
    }
}
