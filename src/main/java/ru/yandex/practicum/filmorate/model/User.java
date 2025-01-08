package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder(toBuilder = true)
@Data
public class User {
    private final Long id;

    private final String email;

    private final String login;

    private final String name;

    private final LocalDate birthday;
}
