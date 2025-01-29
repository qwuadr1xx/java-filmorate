package ru.yandex.practicum.filmorate.storage;

import java.util.List;
import java.util.Optional;

public interface Storage<T> {
    T create(T t);

    T update(T t);

    Optional<T> getById(long id);

    List<T> getAll();

    void isIdExists(long id);
}
