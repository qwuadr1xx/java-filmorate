package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.ValidateReleaseDate;

import java.time.LocalDate;

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
}
