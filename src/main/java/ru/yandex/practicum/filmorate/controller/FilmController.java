package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private long id = 1;

    @GetMapping
    public List<Film> getFilm() {
        log.info("Выведение списка фильмов");
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("Начало добавления фильма");
        long localId = generateId();
        Film localFilm = film.toBuilder().id(localId).build();

        log.debug(localFilm.toString());

        films.put(localId, localFilm);
        return localFilm;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        if (!films.containsKey(film.getId())) {
            log.info("фильма с {} id не существует", film.getId().toString());
            throw new IllegalStateException("Фильм с данным id не существует");
        }

        log.info("Начало обновления фильма");
        log.debug(film.toString());

        films.put(film.getId(), film);
        return film;
    }

    private long generateId() {
        return id++;
    }
}
