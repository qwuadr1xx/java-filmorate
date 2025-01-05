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

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private long id = 1;

    @GetMapping
    public String getFilm() {
        log.info("Выведение списка фильмов");
        return films.values().toString();
    }

    @PostMapping
    public void createFilm(@Valid @RequestBody Film film) {
        log.info("Начало добавления фильма");
        long localId = generateId();
        log.debug(String.valueOf(localId));
        log.debug(film.toString());
        films.put(localId, film.toBuilder().id(localId).build());
    }

    @PutMapping
    public void updateFilm(@Valid @RequestBody Film film) {
        if (films.containsKey(film.getId())) {
            log.info("Начало обновления фильма");
            log.debug(film.toString());
            films.put(film.getId(), film);
        } else {
            log.info("фильм с {} id не существует", film.getId().toString());


        }
    }

    private long generateId() {
        return id++;
    }
}
