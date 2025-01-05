package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmValidationTest {
    private Film film;
    private static Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validateFilmWithoutName() {
        film = Film.builder()
                .id(1L)
                .name("")
                .description("Description1")
                .releaseDate(LocalDate.of(2000, 10, 10))
                .duration(120)
                .build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Название не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void validateFilmWithLongDescription() {
        film = Film.builder()
                .id(1L)
                .name("Film1")
                .description("descriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescription" +
                        "descriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescription" +
                        "descriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescription")
                .releaseDate(LocalDate.of(2000, 10, 10))
                .duration(120)
                .build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Максимальная длина описания - 200 символов", violations.iterator().next().getMessage());
    }

    @Test
    void validateFilmEarlyReleaseDate() {
        film = Film.builder()
                .id(1L)
                .name("Film1")
                .description("Description1")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(120)
                .build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Дата выхода фильма не может быть раньше 28 декабря 1895 года",
                violations.iterator().next().getMessage());
    }

    @Test
    void validateFilmBorderReleaseDate() {
        film = Film.builder()
                .id(1L)
                .name("Film1")
                .description("Description1")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(120)
                .build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(0, violations.size());
    }

    @Test
    void validateFilmWithUpBorderReleaseDate() {
        film = Film.builder()
                .id(1L)
                .name("Film1")
                .description("Description1")
                .releaseDate(LocalDate.of(1895, 12, 29))
                .duration(120)
                .build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(0, violations.size());
    }

    @Test
    void validateFilmWithNegativeDuration() {
        film = Film.builder()
                .id(1L)
                .name("Film1")
                .description("Description1")
                .releaseDate(LocalDate.of(2000, 10, 10))
                .duration(-120)
                .build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Длительность фильма не может быть отрицательной",
                violations.iterator().next().getMessage());
    }

}