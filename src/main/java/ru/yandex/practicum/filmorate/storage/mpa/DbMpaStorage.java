package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Repository
public class DbMpaStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    private final static String GET_ALL_MPA_RATINGS = "SELECT * FROM mpa_ratings GROUP BY id ORDER BY id";

    private final static String GET_MPA_RATING_BY_ID = "SELECT * FROM mpa_ratings WHERE id = ?";

    @Autowired
    public DbMpaStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Mpa> getAllMpaRatings() {
        return jdbcTemplate.query(GET_ALL_MPA_RATINGS, mapRowToMpaRating());
    }

    @Override
    public Mpa getMpaRatingById(long id) {
        return jdbcTemplate.queryForObject(GET_MPA_RATING_BY_ID, mapRowToMpaRating(), id);
    }

    private RowMapper<Mpa> mapRowToMpaRating() {
        return (resultSet, numRow) ->
                Mpa.builder()
                        .id(resultSet.getLong("id"))
                        .name(resultSet.getString("name"))
                        .build();
    }
}