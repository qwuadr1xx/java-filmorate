package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.Storage;

public interface DirectorStorage extends Storage<Director> {

    void delete(Long id);

    void delete(Director director);
}
