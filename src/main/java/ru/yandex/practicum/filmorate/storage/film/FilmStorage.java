package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.enums.FilmSort;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.List;

public interface FilmStorage extends Storage<Film> {
    void addLike(long id, long userId);

    void removeLike(long id, long userId);

    List<Film> commonFilmsList(Long userId, Long friendId);

    List<Film> getPopularFilms(Integer limit, Integer genreId, Integer year);

    List<Film> getFilmsByDirectorWithSort(Long directorId, String sortBy);

    List<Long> getUsersRecommendations(long userId); // Возвращает список ID фильмов для рекомендаций

    List<Film> getFilmsByIds(List<Long> filmIds); // Возвращает список ID фильмов, которые пользователь уже оценил

    List<Long> getFilmsUserById(long userId);

    List<Film> searchFilms(String query, String by, FilmSort sort);
}

