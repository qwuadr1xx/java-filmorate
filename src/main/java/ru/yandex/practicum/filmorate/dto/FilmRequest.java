package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.ValidateReleaseDate;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Builder(toBuilder = true)
@Data
public class FilmRequest {
    private final Long id;

    @NotBlank(message = "Название не может быть пустым")
    private final String name;

    @Size(max = 200, message = "Максимальная длина описания - 200 символов")
    private final String description;

    @ValidateReleaseDate
    private final LocalDate releaseDate;

    @Positive(message = "Длительность фильма не может быть отрицательной")
    private final Integer duration;

    private final Mpa mpa;

    private final List<Genre> genres;

    private final Set<Director> directors;
}
