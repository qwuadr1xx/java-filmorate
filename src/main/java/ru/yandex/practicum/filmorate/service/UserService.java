package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.dto.UserMapper;
import ru.yandex.practicum.filmorate.dto.UserRequest;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService {
    private final Map<Long, User> users;
    private long id;

    public UserService() {
        users = new HashMap<>();
        id = 1L;
    }

    public List<User> getUsersService() {
        return new ArrayList<>(users.values());
    }

    public User createUserService(UserRequest userRequest) {
        User user = UserMapper.mapUserFromDto(userRequest);

        long localId = generateId();
        User localUser = user.toBuilder().id(localId).build();

        users.put(localId, localUser);
        return localUser;
    }

    public User updateUserService(UserRequest userRequest) {
        User user = UserMapper.mapUserFromDto(userRequest);

        if (!users.containsKey(user.getId())) {
            throw new IllegalStateException("Пользователя с данным id не существует");
        }

        users.put(user.getId(), user);
        return user;
    }

    private long generateId() {
        return id++;
    }
}
