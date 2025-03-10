package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.List;

public interface FilmStorage extends Storage<Film> {
    void addLike(long id, long userId);

    void removeLike(long id, long userId);

    List<Film> getLikedFilms(int limit);

    List<Film> commonFilmsList(Long userId, Long friendId);
}
