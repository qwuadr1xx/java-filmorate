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
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmService filmService = new FilmService();

    @GetMapping
    public List<Film> getFilms() {
        log.info("Выведение списка фильмов");

        return filmService.getFilmsService();
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("Начало добавления фильма");

        return filmService.createFilmService(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Начало обновления фильма");

        return filmService.updateFilmService(film);
    }
}
