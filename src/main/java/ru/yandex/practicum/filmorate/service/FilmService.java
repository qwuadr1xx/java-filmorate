package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmMapper;
import ru.yandex.practicum.filmorate.dto.FilmRequest;
import ru.yandex.practicum.filmorate.enums.Entity;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.DbFilmStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.DbUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class FilmService {
    private final FilmStorage dbFilmStorage;
    private final UserStorage dbUserStorage;

    @Autowired
    public FilmService(DbFilmStorage dbFilmStorage, DbUserStorage dbUserStorage) {
        this.dbFilmStorage = dbFilmStorage;
        this.dbUserStorage = dbUserStorage;
    }

    public List<Film> getFilms() {
        return dbFilmStorage.getAll();
    }

    public Film getFilmById(long id) {
        validateId(id);

        return dbFilmStorage.getById(id);
    }

    public Film createFilm(FilmRequest filmRequest) {
        Film film = FilmMapper.mapFilmFromDto(filmRequest);

        return dbFilmStorage.create(film);
    }

    public Film updateFilm(FilmRequest filmRequest) {
        Film film = FilmMapper.mapFilmFromDto(filmRequest);

        return dbFilmStorage.update(film);
    }

    public void addLike(long id, long userId) {
        validateId(id);
        validateId(userId);

        dbFilmStorage.addLike(id, userId);
    }

    public void removeLike(long id, long userId) {
        validateId(id);
        validateId(userId);

        dbFilmStorage.removeLike(id, userId);
    }

    public void deleteFilm(long id) {
        validateId(id);

        dbFilmStorage.deleteById(id);
    }

    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        if (count < 0) {
            throw new BadRequestException("Значение count не может быть отрицательным", Entity.FILM);
        } else if (count == 0) {
            throw new BadRequestException("Значение count не может быть равным нулю", Entity.FILM);
        }

        return dbFilmStorage.getPopularFilms(count, genreId, year);
    }

    private static void validateId(long id) {
        if (id < 0) {
            throw new BadRequestException("id не может быть отрицательным", Entity.FILM);
        } else if (id == 0) {
            throw new BadRequestException("id не может быть равным нулю", Entity.FILM);
        }
    }

    public List<Film> commonFilmsList(Long userId, Long friendId) {
        if (dbUserStorage.getById(userId) == null || dbUserStorage.getById(friendId) == null) {
            throw new BadRequestException("Один из пользователей не зарегестрирован", Entity.USER);
        } else {
            return dbFilmStorage.commonFilmsList(userId, friendId);
        }
    }

    public List<Film> getFilmsByDirectorWithSort(int directorId, String sortBy) {
        return dbFilmStorage.getFilmsByDirectorWithSort(directorId, sortBy);

    }

    public List<Film> searchFilms(String query, String by, String sort) {
        if (!"year".equals(sort) && !"likes".equals(sort)) {
            throw new BadRequestException("Некорректный параметр сортировки. Используйте 'year' или 'likes'.", Entity.FILM);
        }
        return dbFilmStorage.searchFilms(query, by, sort);
    }
}

