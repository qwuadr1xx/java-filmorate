package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.List;

public interface FilmStorage extends Storage<Film> {
    void addLike(long id, long userId);

    void removeLike(long id, long userId);

    List<Film> getLikedFilms(int limit);

    List<Film> commonFilmsList(Long userId, Long friendId);

    List<Film> getPopularFilms(Integer limit, Integer genreId, Integer year);

    List<Film> getFilmsByDirectorWithSort(int directorId, String sortBy);

    List<Long> getUsersRecommendations(long userId); // Возвращает список ID фильмов для рекомендаций

    List<Film> getFilmsByIds(List<Long> filmIds); // Возвращает список ID фильмов, которые пользователь уже оценил

    List<Long> getFilmsUserById(long userId);

}