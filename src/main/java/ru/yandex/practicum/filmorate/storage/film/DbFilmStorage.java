package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.enums.Entity;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class DbFilmStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    private static final String CREATE_FILM = "INSERT INTO films(name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_ALL_FILMS = "SELECT f.id, f.name, f.description, f.duration, f.release_date, f.mpa_rating_id, m.name AS mpa_rating_name " +
            "FROM films AS f " +
            "INNER JOIN mpa_ratings AS m ON f.mpa_rating_id = m.id";

    private static final String SELECT_FILM_BY_ID = SELECT_ALL_FILMS + " WHERE f.id = ?";

    private static final String UPDATE_FILM = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE id = ?";

    private static final String ADD_LIKE = "INSERT INTO likes(film_id, user_id) VALUES (?, ?)";

    private static final String DELETE_LIKE = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    private static final String DELETE_FILM = "DELETE FROM films WHERE id = ?";

    private static final String DELETE_LIKES_BY_FILM = "DELETE FROM likes WHERE film_id = ?";

    private static final String DELETE_GENRES_BY_FILM = "DELETE FROM film_genres WHERE film_id = ?";

    private static final String GET_LIKED_FILMS = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name AS mpa_rating_name
            FROM films AS f
            INNER JOIN likes AS l ON f.id = l.film_id
            INNER JOIN mpa_ratings AS m ON f.mpa_rating_id = m.id
            """;

    private static final String GET_LIKED_FILMS_GROUP_AND_SORT = """
            GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name
            ORDER BY COUNT(l.user_id) DESC LIMIT ?
            """;

    private static final String GET_USER_BY_ID = "SELECT * FROM users WHERE id = ?";

    private static final String GET_GENRES_BY_FILM_ID = "SELECT g.id, g.name " +
            "FROM genres AS g " +
            "INNER JOIN film_genres AS fg ON g.id = fg.genre_id " +
            "WHERE fg.film_id = ?";

    @Autowired
    public DbFilmStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> getAll() {
        log.debug("Получение всех фильмов из базы данных");
        List<Film> films = jdbcTemplate.query(SELECT_ALL_FILMS, mapRowToFilm());
        films = films.stream()
                .map(film -> film.toBuilder().genres(getGenresForFilm(film.getId())).build())
                .collect(Collectors.toList());
        log.info("Получено {} фильмов", films.size());
        return films;
    }

    @Override
    public Film getById(long id) {
        log.debug("Получение фильма с id: {}", id);
        Film film;
        try {
            film = jdbcTemplate.queryForObject(SELECT_FILM_BY_ID, mapRowToFilm(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(id, Entity.FILM);
        }
        Set<Genre> genres = getGenresForFilm(id);
        film = film.toBuilder().genres(genres).build();
        log.info("Получен фильм: {}", film);
        return film;
    }

    @Override
    public Film create(Film film) {
        log.debug("Создание фильма: {}", film);
        checkMpaExists(film.getMpa().getId());
        checkGenresExist(film.getGenres());
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(CREATE_FILM, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setLong(4, film.getDuration());
            stmt.setLong(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        Film createdFilm = film.toBuilder()
                .id(keyHolder.getKey().longValue())
                .build();
        updateGenres(createdFilm.getGenres(), createdFilm.getId());
        createdFilm = createdFilm.toBuilder().genres(getGenresForFilm(createdFilm.getId())).build();
        log.info("Фильм создан: {}", createdFilm);
        return createdFilm;
    }

    @Override
    public Film update(Film film) {
        getById(film.getId());
        checkMpaExists(film.getMpa().getId());
        checkGenresExist(film.getGenres());

        log.debug("Обновление фильма: {}", film);
        jdbcTemplate.update(UPDATE_FILM, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), film.getId());
        updateGenres(film.getGenres(), film.getId());
        log.info("Фильм обновлён с id: {}", film.getId());
        return film;
    }

    @Override
    public void addLike(long id, long userId) {
        getById(id);
        checkIsUserExist(userId);
        log.debug("Добавление лайка: фильм id {} от пользователя id {}", id, userId);
        jdbcTemplate.update(ADD_LIKE, id, userId);
        log.info("Лайк добавлен: фильм id {} от пользователя id {}", id, userId);
    }

    @Override
    public void removeLike(long id, long userId) {
        getById(id);
        checkIsUserExist(userId);
        log.debug("Удаление лайка: фильм id {} от пользователя id {}", id, userId);
        jdbcTemplate.update(DELETE_LIKE, id, userId);
        log.info("Лайк удалён: фильм id {} от пользователя id {}", id, userId);
    }

    @Override
    public List<Film> getPopularFilms(Integer limit, Integer genreId, Integer year) {
        log.debug("Получение топ-{} фильмов по лайкам", limit);

        List<Object> args = new ArrayList<>();

        String joinsForQuery = "";
        if (genreId != null) {
            joinsForQuery = "JOIN film_genres fg ON f.id = fg.film_id and fg.genre_id = ? ";
            args.add(genreId);
        }

        String whereForQuery = "";
        if (year != null) {
            whereForQuery = "WHERE EXTRACT(YEAR FROM (f.release_date)) = ? ";
            args.add(year);
        }

        args.add(limit);

        List<Film> films = jdbcTemplate.query(GET_LIKED_FILMS + joinsForQuery + whereForQuery + GET_LIKED_FILMS_GROUP_AND_SORT, mapRowToFilm(), args.toArray());
        films = films.stream()
                .map(film -> film.toBuilder().genres(getGenresForFilm(film.getId())).build())
                .collect(Collectors.toList());
        log.info("Получено {} фильмов", films.size());
        return films;
    }

    @Override
    public void deleteById(long id) {
        log.debug("Удаление фильма с id {}", id);

        jdbcTemplate.update(DELETE_FILM, id);
        jdbcTemplate.update(DELETE_LIKES_BY_FILM, id);
        jdbcTemplate.update(DELETE_GENRES_BY_FILM, id);

        log.info("Фильм {} удален", id);
    }

    private static RowMapper<Film> mapRowToFilm() {
        return (rs, rowNum) ->
                Film.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .releaseDate(rs.getDate("release_date").toLocalDate())
                        .duration(rs.getInt("duration"))
                        .mpa(Mpa.builder()
                                .id(rs.getLong("mpa_rating_id"))
                                .name(rs.getString("mpa_rating_name"))
                                .build())
                        .genres(Collections.emptySet())
                        .build();
    }

    private Set<Genre> getGenresForFilm(long filmId) {
        List<Genre> genreList = jdbcTemplate.query(
                GET_GENRES_BY_FILM_ID,
                (rs, rowNum) -> Genre.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .build(),
                filmId);
        return genreList.stream()
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Genre::getId))));
    }

    private void updateGenres(Set<Genre> genres, long filmId) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", filmId);
        if (genres != null && !genres.isEmpty()) {
            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            Set<Genre> sortedGenres = genres.stream()
                    .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Genre::getId))));
            List<Object[]> batchArgs = sortedGenres.stream()
                    .map(genre -> new Object[]{filmId, genre.getId()})
                    .collect(Collectors.toList());
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }
    }

    private void checkIsUserExist(long userId) {
        log.debug("Проверка существования пользователя с id: {}", userId);
        try {
            jdbcTemplate.queryForObject(GET_USER_BY_ID, (rs, rowNum) -> 1, userId);
            log.debug("Пользователь с id {} существует", userId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(userId, Entity.USER);
        }
    }

    private void checkMpaExists(long mpaId) {
        try {
            jdbcTemplate.queryForObject("SELECT 1 FROM mpa_ratings WHERE id = ?", Integer.class, mpaId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(mpaId, Entity.MPA_RATING);
        }
    }

    private void checkGenresExist(Set<Genre> genres) {
        if (genres != null) {
            for (Genre genre : genres) {
                try {
                    jdbcTemplate.queryForObject("SELECT 1 FROM genres WHERE id = ?", Integer.class, genre.getId());
                } catch (EmptyResultDataAccessException e) {
                    throw new NotFoundException(genre.getId(), Entity.GENRE);
                }
            }
        }
    }
}
