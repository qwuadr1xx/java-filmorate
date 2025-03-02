package ru.yandex.practicum.filmorate.dto;

import ru.yandex.practicum.filmorate.model.Film;

public final class FilmMapper {
    private FilmMapper() {

    }

    public static Film mapFilmFromDto(FilmRequest filmRequest) {
        return Film.builder()
                .id(filmRequest.getId())
                .name(filmRequest.getName())
                .description(filmRequest.getDescription())
                .releaseDate(filmRequest.getReleaseDate())
                .duration(filmRequest.getDuration())
                .mpa(filmRequest.getMpa())
                .genres(filmRequest.getGenres())
                .build();
    }
}
