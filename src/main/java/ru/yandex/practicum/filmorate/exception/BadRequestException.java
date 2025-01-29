package ru.yandex.practicum.filmorate.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.enums.Entity;

@Getter
@RequiredArgsConstructor
public class BadRequestException extends RuntimeException {
    private final String error;
    private final Entity entity;
}
