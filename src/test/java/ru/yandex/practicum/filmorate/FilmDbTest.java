package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.feed.DbFeedStorage;
import ru.yandex.practicum.filmorate.storage.film.DbFilmStorage;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({DbFilmStorage.class, DbFeedStorage.class})
class FilmDbTest {

    @Autowired
    private DbFilmStorage filmStorage;

    @Test
    void testCreateAndFindFilm() {
        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1L).build())
                .genres(null)
                .directors(null)
                .build();
        Film createdFilm = filmStorage.create(film);
        Film foundFilm = filmStorage.getById(createdFilm.getId());
        assertThat(foundFilm).isNotNull();
        assertThat(foundFilm.getId()).isEqualTo(createdFilm.getId());
        assertThat(foundFilm.getName()).isEqualTo("Test Film");
    }

    @Test
    void testUpdateFilm() {
        Film film = Film.builder()
                .name("Original Film")
                .description("Original Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .mpa(Mpa.builder().id(1L).build())
                .genres(null)
                .directors(null)
                .build();
        Film createdFilm = filmStorage.create(film);
        Film updatedFilm = createdFilm.toBuilder()
                .name("Updated Film")
                .description("Updated Description")
                .duration(150)
                .build();
        filmStorage.update(updatedFilm);
        Film foundFilm = filmStorage.getById(createdFilm.getId());
        assertThat(foundFilm.getName()).isEqualTo("Updated Film");
        assertThat(foundFilm.getDuration()).isEqualTo(150);
    }
}
