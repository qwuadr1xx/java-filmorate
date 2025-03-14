package ru.yandex.practicum.filmorate.dto;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collections;
import java.util.List;

public final class FilmMapper {
    private FilmMapper() {

    }

    public static Film mapFilmFromDto(FilmRequest filmRequest) {
        List<Genre> genres;

        if (filmRequest.getGenres() != null && !filmRequest.getGenres().isEmpty()) {
            genres = filmRequest.getGenres().stream().distinct().toList();
        } else {
            genres = Collections.emptyList();
        }

        return Film.builder()
                .id(filmRequest.getId())
                .name(filmRequest.getName())
                .description(filmRequest.getDescription())
                .releaseDate(filmRequest.getReleaseDate())
                .duration(filmRequest.getDuration())
                .mpa(filmRequest.getMpa())
                .genres(genres)
                .directors(filmRequest.getDirectors())
                .build();
    }
}
