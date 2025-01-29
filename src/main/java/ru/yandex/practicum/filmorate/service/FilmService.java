package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmMapper;
import ru.yandex.practicum.filmorate.dto.FilmRequest;
import ru.yandex.practicum.filmorate.enums.Entity;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage inMemoryFilmStorage;
    private final UserService userService;

    public List<Film> getFilms() {
        return new ArrayList<>(inMemoryFilmStorage.getAll());
    }

    public Film getFilmById(long id) {
        return inMemoryFilmStorage.getById(id).orElseThrow(() -> new NotFoundException(id, Entity.FILM));
    }

    public Film createFilm(FilmRequest filmRequest) {
        Film film = FilmMapper.mapFilmFromDto(filmRequest);

        return inMemoryFilmStorage.create(film);
    }

    public Film updateFilm(FilmRequest filmRequest) {
        Film film = FilmMapper.mapFilmFromDto(filmRequest);

        return inMemoryFilmStorage.update(film);
    }

    public void addLike(long id, long userId) {
        userService.isUserIdExists(userId);

        inMemoryFilmStorage.addLike(id, userId);
    }

    public void removeLike(long id, long userId) {
        userService.isUserIdExists(userId);

        inMemoryFilmStorage.removeLike(id, userId);
    }

    public List<Film> getPopularFilms(int count) {
        if (count < 0) {
            throw new BadRequestException("Значение count не может быть отрицательным", Entity.FILM);
        }

        return new ArrayList<>(inMemoryFilmStorage.getLikedFilms().subList(0, Integer.min(inMemoryFilmStorage.getLikedFilms().size(), count)));
    }
}
