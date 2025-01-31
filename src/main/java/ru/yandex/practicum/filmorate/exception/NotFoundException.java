package ru.yandex.practicum.filmorate.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.enums.Entity;

@Getter
@RequiredArgsConstructor
public class NotFoundException extends RuntimeException {
    private final long id;
    private final Entity entity;
}
