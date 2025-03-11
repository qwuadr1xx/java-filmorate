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
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.FeedRecord;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.feed.DbFeedStorage;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class DbFilmStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FeedStorage dbFeedStorage;
    private final DirectorStorage directorStorage;

    private static final String CREATE_FILM = "INSERT INTO films(name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_ALL_FILMS = "SELECT f.id, f.name, f.description, f.duration, f.release_date, f.mpa_rating_id, m.name AS mpa_rating_name " +
            "FROM films AS f " +
            "INNER JOIN mpa_ratings AS m ON f.mpa_rating_id = m.id ";

    private static final String SELECT_FILM_BY_ID = SELECT_ALL_FILMS + " WHERE f.id = ?";

    private static final String UPDATE_FILM = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE id = ?";

    private static final String ADD_LIKE = "INSERT INTO likes (film_id, user_id) " +
            "SELECT ?, ? FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM likes WHERE film_id = ? AND user_id = ?)";

    private static final String DELETE_LIKE = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    private static final String DELETE_FILM = "DELETE FROM films WHERE id = ?";

    private static final String DELETE_LIKES_BY_FILM = "DELETE FROM likes WHERE film_id = ?";

    private static final String DELETE_GENRES_BY_FILM = "DELETE FROM film_genres WHERE film_id = ?";

    private static final String GET_LIKED_FILMS = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name AS mpa_rating_name
            FROM films AS f
            LEFT JOIN likes AS l ON f.id = l.film_id
            LEFT JOIN mpa_ratings AS m ON f.mpa_rating_id = m.id
            """;

    private static final String GET_LIKED_FILMS_GROUP_AND_SORT = """
            GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name
            ORDER BY COUNT(l.user_id) DESC
            """;

    private static final String GET_USER_BY_ID = "SELECT * FROM users WHERE id = ?";

    private static final String GET_GENRES_BY_FILM_ID = "SELECT g.id, g.name " +
            "FROM genres AS g " +
            "INNER JOIN film_genres AS fg ON g.id = fg.genre_id " +
            "WHERE fg.film_id = ?";

    private static final String GET_DIRECTORS_BY_FILM_ID = "SELECT d.id, d.name " +
            "FROM directors AS d " +
            "INNER JOIN film_director AS fd ON d.id = fd.director_id " +
            "WHERE fd.film_id = ?";

    private static final String GET_FILMS_BY_USER_ID = "SELECT film_id FROM likes WHERE user_id = ?";

    private static final String GET_USERS_RECOMMENDATIONS = """
            SELECT l.film_id
            FROM likes l
            WHERE l.user_id IN (
                SELECT l2.user_id
                FROM likes l1
                JOIN likes l2 ON l1.film_id = l2.film_id
                WHERE l1.user_id = ? AND l2.user_id != ?
            )
            AND l.film_id NOT IN (
                SELECT film_id
                FROM likes
                WHERE user_id = ?
            )
            GROUP BY l.film_id
            ORDER BY COUNT(l.user_id) DESC
            """;

    private static final String GET_COMMON_FILMS_LIST = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name AS mpa_rating_name " +
            "FROM films AS f " +
            "JOIN likes l1 ON f.id = l1.film_id AND l1.user_id = ? " +
            "JOIN likes l2 ON f.id = l2.film_id AND l2.user_id = ? " +
            "JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
            "ORDER BY (SELECT COUNT(*) FROM likes l WHERE l.film_id = f.id) DESC";

    private static final String GET_SORT_FILMS = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name AS mpa_rating_name " +
            "FROM films AS f " +
            "INNER JOIN mpa_ratings AS m ON f.mpa_rating_id = m.id " +
            "LEFT JOIN film_director fd ON f.id = fd.film_id " +
            "LEFT JOIN directors d ON fd.director_id = d.id " +
            "LEFT JOIN likes l ON f.id = l.film_id ";

    private static final String GET_SORT_FILMS_GROUP = "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name " +
            "ORDER BY " +
            "CASE WHEN ? = 'year' THEN f.release_date END DESC, " +
            "CASE WHEN ? = 'likes' THEN COUNT(DISTINCT l.user_id) END DESC";

    @Autowired
    public DbFilmStorage(JdbcTemplate jdbcTemplate, DbFeedStorage dbFeedStorage, DirectorStorage directorStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.dbFeedStorage = dbFeedStorage;
        this.directorStorage = directorStorage;
    }

    @Override
    public List<Long> getFilmsUserById(long userId) {
        return jdbcTemplate.query(GET_FILMS_BY_USER_ID, (rs, rowNum) -> rs.getLong("film_id"), userId);
    }

    @Override
    public List<Long> getUsersRecommendations(long userId) {
        return jdbcTemplate.query(
                GET_USERS_RECOMMENDATIONS,
                (rs, rowNum) -> rs.getLong("film_id"),
                userId, userId, userId
        );
    }

    @Override
    public List<Film> getFilmsByIds(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return List.of();
        }
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name AS mpa_rating_name " +
                "FROM films AS f " +
                "INNER JOIN mpa_ratings AS m ON f.mpa_rating_id = m.id " +
                "WHERE f.id IN (" + String.join(",", Collections.nCopies(filmIds.size(), "?")) + ")";
        return jdbcTemplate.query(sql, mapRowToFilm(), filmIds.toArray()).stream()
                .map(film -> film.toBuilder()
                        .genres(getGenresForFilm(film.getId()))
                        .directors(getDirectorsForFilm(film.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<Film> getAll() {
        log.debug("Получение всех фильмов из базы данных");
        List<Film> films = jdbcTemplate.query(SELECT_ALL_FILMS, mapRowToFilm());
        films = films.stream()
                .map(film -> film.toBuilder()
                        .genres(getGenresForFilm(film.getId()))
                        .directors(getDirectorsForFilm(film.getId()))
                        .build())
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
        film = film.toBuilder()
                .genres(getGenresForFilm(id))
                .directors(getDirectorsForFilm(id))
                .build();
        log.info("Получен фильм: {}", film);
        return film;
    }

    @Override
    public Film create(Film film) {
        log.debug("Создание фильма: {}", film);
        checkMpaExists(film.getMpa().getId());
        checkGenresExist(film.getGenres());
        checkDirectorsExist(film.getDirectors());
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

        if (film.getGenres() != null) {
            updateGenres(createdFilm.getGenres(), createdFilm.getId());
        }
        updateFilmDirectors(createdFilm.getDirectors(), createdFilm.getId());

        createdFilm = createdFilm.toBuilder()
                .genres(getGenresForFilm(createdFilm.getId()))
                .directors(getDirectorsForFilm(createdFilm.getId()))
                .build();
        log.info("Фильм создан: {}", createdFilm);
        return createdFilm;
    }

    @Override
    public Film update(Film film) {
        getById(film.getId());
        checkMpaExists(film.getMpa().getId());
        checkGenresExist(film.getGenres());
        checkDirectorsExist(film.getDirectors());

        log.debug("Обновление фильма: {}", film);
        jdbcTemplate.update(UPDATE_FILM, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), film.getId());
        if (film.getGenres() != null) {
            updateGenres(film.getGenres(), film.getId());
        }
        updateFilmDirectors(film.getDirectors(), film.getId());

        log.info("Фильм обновлён с id: {}", film.getId());
        return film;
    }

    @Override
    public void addLike(long id, long userId) {
        getById(id);
        checkIsUserExist(userId);
        log.debug("Добавление лайка: фильм id {} от пользователя id {}", id, userId);
        jdbcTemplate.update(ADD_LIKE, id, userId, id, userId);
        log.info("Лайк добавлен: фильм id {} от пользователя id {}", id, userId);
        dbFeedStorage.setRecord(FeedRecord.builder()
                .timestamp(Instant.now().toEpochMilli())
                .userId(userId)
                .eventType(EventType.LIKE)
                .operation(Operation.ADD)
                .entityId(id)
                .build());
    }

    @Override
    public void removeLike(long id, long userId) {
        getById(id);
        checkIsUserExist(userId);
        log.debug("Удаление лайка: фильм id {} от пользователя id {}", id, userId);
        jdbcTemplate.update(DELETE_LIKE, id, userId);
        log.info("Лайк удалён: фильм id {} от пользователя id {}", id, userId);
        dbFeedStorage.setRecord(FeedRecord.builder()
                .timestamp(Instant.now().toEpochMilli())
                .userId(userId)
                .eventType(EventType.LIKE)
                .operation(Operation.REMOVE)
                .entityId(id)
                .build());
    }

    @Override
    public List<Film> getPopularFilms(Integer limit, Integer genreId, Integer year) {
        log.debug("Получение топ-{} фильмов по лайкам", limit);

        List<Object> args = new ArrayList<>();

        String joinsForQuery = "";
        if (genreId != null) {
            joinsForQuery = " JOIN film_genres fg ON f.id = fg.film_id and fg.genre_id = ? ";
            args.add(genreId);
        }

        String whereForQuery = "";
        if (year != null) {
            whereForQuery = "WHERE EXTRACT(YEAR FROM (f.release_date)) = ? ";
            args.add(year);
        }

        String limitForQuery = "";
        if (limit != null) {
            limitForQuery = " LIMIT ?";
            args.add(limit);
        }

        List<Film> films = jdbcTemplate.query(GET_LIKED_FILMS + joinsForQuery + whereForQuery + GET_LIKED_FILMS_GROUP_AND_SORT + limitForQuery, mapRowToFilm(), args.toArray());
        films = films.stream()
                .map(film -> film.toBuilder()
                        .genres(getGenresForFilm(film.getId()))
                        .directors(getDirectorsForFilm(film.getId()))
                        .build())
                .collect(Collectors.toList());
        log.info("Получено {} фильмов", films.size());
        return films;
    }

    @Override
    public List<Film> getFilmsByDirectorWithSort(int directorId, String sortBy) {

        directorStorage.getById(directorId);

        String joinsForQuery = "JOIN film_director fd on f.id = fd.film_id ";
        String whereForQuery = "WHERE fd.director_id = ? ";
        String groupByForQuery = "";
        String orderByForQuery = "";
        switch (sortBy) {
            case "year":
                orderByForQuery = "ORDER BY f.release_date ";
                break;
            case "likes":
                joinsForQuery += "LEFT JOIN likes l ON f.id = l.film_id ";
                groupByForQuery = "GROUP BY f.id ";
                orderByForQuery = "ORDER BY COUNT(l.user_id) DESC ";
                break;
        }

        return jdbcTemplate.query(SELECT_ALL_FILMS
                                + joinsForQuery + whereForQuery
                                + groupByForQuery + orderByForQuery,
                        mapRowToFilm(), directorId)
                .stream()
                .map(film -> film.toBuilder()
                        .genres(getGenresForFilm(film.getId()))
                        .directors(getDirectorsForFilm(film.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(long id) {
        log.debug("Удаление фильма с id {}", id);

        jdbcTemplate.update(DELETE_LIKES_BY_FILM, id);
        jdbcTemplate.update(DELETE_GENRES_BY_FILM, id);
        jdbcTemplate.update(DELETE_FILM, id);

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
                        .genres(Collections.emptyList())
                        .build();
    }

    private List<Genre> getGenresForFilm(long filmId) {
        List<Genre> genreList = jdbcTemplate.query(
                GET_GENRES_BY_FILM_ID,
                (rs, rowNum) -> Genre.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .build(),
                filmId);
        return genreList.stream()
                .sorted(Comparator.comparing(Genre::getId))
                .collect(Collectors.toList());
    }

    private void updateGenres(List<Genre> genres, long filmId) {
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

    private Set<Director> getDirectorsForFilm(long filmId) {
        List<Director> directorList = jdbcTemplate.query(
                GET_DIRECTORS_BY_FILM_ID,
                (rs, rowNum) -> Director.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .build(),
                filmId);
        return directorList.stream()
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Director::getId))));
    }

    private void updateFilmDirectors(Set<Director> directors, long filmId) {
        jdbcTemplate.update("DELETE FROM film_director WHERE film_id = ?", filmId);
        if (directors != null && !directors.isEmpty()) {
            String sql = "INSERT INTO film_director (film_id, director_id) VALUES (?, ?)";
            Set<Director> sortedDirectors = directors.stream()
                    .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Director::getId))));
            List<Object[]> batchArgs = sortedDirectors.stream()
                    .map(director -> new Object[]{filmId, director.getId()})
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

    private void checkGenresExist(List<Genre> genres) {
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

    @Override
    public List<Film> commonFilmsList(Long userId, Long friendId) {
        log.info("Получение общих фильмов для userId={} и friendId={}", userId, friendId);

        checkIsUserExist(userId);
        checkIsUserExist(friendId);

        List<Film> commonFilms = jdbcTemplate.query(GET_COMMON_FILMS_LIST, mapRowToFilm(), userId, friendId);

        commonFilms = commonFilms.stream()
                .map(film -> film.toBuilder().genres(getGenresForFilm(film.getId())).build())
                .collect(Collectors.toList());

        log.info("Найдено {} общих фильмов", commonFilms.size());
        return commonFilms;
    }

    private void checkDirectorsExist(Set<Director> directors) {
        if (directors != null) {
            for (Director director : directors) {
                try {
                    jdbcTemplate.queryForObject("SELECT 1 FROM directors WHERE id = ?", Integer.class, director.getId());
                } catch (EmptyResultDataAccessException e) {
                    throw new NotFoundException(director.getId(), Entity.DIRECTOR);
                }
            }
        }
    }

    @Override
    public List<Film> searchFilms(String query, String by, String sort) {
        log.debug("Поиск фильмов по запросу: '{}', по параметрам: '{}', сортировка: '{}'", query, by, sort);

        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String searchQuery = "%" + query.toLowerCase() + "%";

        boolean searchByTitle = by.contains("title");
        boolean searchByDirector = by.contains("director");

        List<Object> args = new ArrayList<>();
        String whereForQuery = "";

        List<Film> films;
        if (searchByTitle && searchByDirector) {
            whereForQuery = "WHERE LOWER(f.name) LIKE LOWER(?) OR LOWER(d.name) LIKE LOWER(?) ";
            args.add(searchQuery);
            args.add(searchQuery);
        } else if (searchByTitle) {
            whereForQuery = "WHERE LOWER(f.name) LIKE LOWER(?) ";
            args.add(searchQuery);
        } else if (searchByDirector) {
            whereForQuery = "WHERE LOWER(d.name) LIKE LOWER(?) ";
            args.add(searchQuery);
        } else {
            throw new IllegalArgumentException("Некорректный параметр 'by'. Используйте 'title', 'director' или 'title,director'.");
        }
        args.add(sort);
        args.add(sort);

        films = jdbcTemplate.query(GET_SORT_FILMS + whereForQuery + GET_SORT_FILMS_GROUP, mapRowToFilm(), args.toArray());

        films = films.stream()
                .map(film -> film.toBuilder()
                        .genres(getGenresForFilm(film.getId()))
                        .directors(getDirectorsForFilm(film.getId()))
                        .build())
                .collect(Collectors.toList());

        log.info("Найдено {} фильмов по запросу: '{}'", films.size(), query);
        return films;
    }
}