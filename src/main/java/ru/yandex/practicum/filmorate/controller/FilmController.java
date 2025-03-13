package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.FilmRequest;
import ru.yandex.practicum.filmorate.enums.FilmSort;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
@Slf4j
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public List<Film> getFilms() {
        log.info("Выведение списка фильмов");

        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable long id) {
        log.info("Выведение фильма c id {}", id);

        return filmService.getFilmById(id);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(value = "count", required = false) Integer count,
                                      @RequestParam(value = "genreId", required = false) Integer genreId,
                                      @RequestParam(value = "year", required = false) Integer year) {
        log.info("Вывод популярных фильмов с фильтром по жанру и году");

        return filmService.getPopularFilms(count, genreId, year);
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody final FilmRequest filmRequest) {
        log.info("Начало добавления фильма");

        return filmService.createFilm(filmRequest);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable final long id, @PathVariable final long userId) {
        log.info("Добавление лайка");

        filmService.addLike(id, userId);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody final FilmRequest filmRequest) {
        log.info("Начало обновления фильма");

        return filmService.updateFilm(filmRequest);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable final long id, @PathVariable final long userId) {
        log.info("Удаление лайка");

        filmService.removeLike(id, userId);
    }

    @GetMapping("/common")
    public List<Film> commonFilmsList(@RequestParam Long userId, @RequestParam Long friendId) {
        log.info("Получен запрос GET /films/common?userId={}&friendId={}", userId, friendId);
        return filmService.commonFilmsList(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(@PathVariable final Long directorId, @RequestParam final String sortBy) {
        log.info("Вывод фильмов режиссера {}", directorId);

        return filmService.getFilmsByDirectorWithSort(directorId, sortBy);
    }

    @DeleteMapping("/{filmId}")
    public void deleteFilm(@PathVariable final long filmId) {
        log.info("Удаление фильма");

        filmService.deleteFilm(filmId);

    }

    @GetMapping("/search")
    public List<Film> searchFilms(
            @RequestParam String query,
            @RequestParam(defaultValue = "title") String by,
            @RequestParam(defaultValue = "likes") FilmSort sort) {
        return filmService.searchFilms(query, by, sort);
    }
}
