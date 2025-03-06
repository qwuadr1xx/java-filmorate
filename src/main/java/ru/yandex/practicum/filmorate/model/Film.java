package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Builder(toBuilder = true)
@Data
public class Film {
    private final Long id;

    private final String name;

    private final String description;

    private final LocalDate releaseDate;

    private final Integer duration;

    private final Mpa mpa;

    private final Set<Genre> genres;

    private final Set<Director> directors;
}
