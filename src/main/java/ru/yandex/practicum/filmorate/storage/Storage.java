package ru.yandex.practicum.filmorate.storage;

import java.util.List;

public interface Storage<T> {
    T create(T t);

    T update(T t);

    T getById(long id);

    List<T> getAll();

    void deleteById(long id);
}
