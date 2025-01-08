package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilmService {
    private final Map<Long, Film> films;
    private long id;

    public FilmService() {
        films = new HashMap<>();
        id = 1L;
    }

    public List<Film> getFilmsService() {
        return new ArrayList<>(films.values());
    }

    public Film createFilmService(Film film) {
        long localId = generateId();
        Film localFilm = film.toBuilder().id(localId).build();

        films.put(localId, localFilm);
        return localFilm;
    }

    public Film updateFilmService(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new IllegalStateException("Фильм с данным id не существует");
        }

        films.put(film.getId(), film);
        return film;
    }

    private long generateId() {
        return id++;
    }
}
