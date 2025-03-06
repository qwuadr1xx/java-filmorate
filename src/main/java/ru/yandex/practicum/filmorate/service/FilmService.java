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

import java.util.List;

@Service
public class FilmService {
    private final FilmStorage dbFilmStorage;

    @Autowired
    public FilmService(DbFilmStorage dbFilmStorage) {
        this.dbFilmStorage = dbFilmStorage;
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

    public List<Film> getFilmsByDirectorWithSort(int directorId, String sortBy) {
        return dbFilmStorage.getFilmsByDirectorWithSort(directorId, sortBy);
    }
}
