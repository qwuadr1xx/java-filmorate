package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Repository
public class DbGenreStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    private final static String GET_ALL_GENRES = "SELECT * FROM genres GROUP BY id ORDER BY id";

    private final static String GET_GENRE_BY_ID = "SELECT * FROM genres WHERE id = ?";

    @Autowired
    public DbGenreStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genre> getAllGenres() {
        return jdbcTemplate.query(GET_ALL_GENRES, mapRowToGenre());
    }

    @Override
    public Genre getGenreById(long id) {
        return jdbcTemplate.queryForObject(GET_GENRE_BY_ID, mapRowToGenre(), id);
    }

    private RowMapper<Genre> mapRowToGenre() {
        return (resultSet, numRow) ->
                Genre.builder()
                        .id(resultSet.getLong("id"))
                        .name(resultSet.getString("name"))
                        .build();
    }
}