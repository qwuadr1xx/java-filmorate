package ru.yandex.practicum.filmorate.dto;

import ru.yandex.practicum.filmorate.model.User;

public final class UserMapper {
    private UserMapper() {

    }

    public static User mapUserFromDto(UserRequest userRequest) {
        return User.builder()
                .id(userRequest.getId())
                .email(userRequest.getEmail())
                .login(userRequest.getLogin())
                .name((userRequest.getName() == null || userRequest.getName().isBlank()) ? userRequest.getLogin() : userRequest.getName())
                .birthday(userRequest.getBirthday())
                .build();
    }
}
