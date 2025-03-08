package ru.yandex.practicum.filmorate.storage.director;

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
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.util.List;

@Slf4j
@Repository
public class DbDirectorStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    private static final String SELECT_ALL_DIRECTORS = "SELECT * FROM directors";

    private static final String SELECT_DIRECTOR_BY_ID = "SELECT * FROM directors WHERE id = ?";

    private static final String CREATE_DIRECTOR = "INSERT INTO directors(name) VALUES (?)";

    private static final String UPDATE_DIRECTOR = "UPDATE directors SET name = ? WHERE id = ?";

    private static final String DELETE_DIRECTOR = "DELETE FROM directors WHERE id = ?";

    @Autowired
    public DbDirectorStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Director getById(long id) {
        log.debug("Получение режиссера с id: {}", id);
        try {
            return jdbcTemplate.queryForObject(SELECT_DIRECTOR_BY_ID, mapRowToDirector(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(id, Entity.DIRECTOR);
        }
    }

    @Override
    public List<Director> getAll() {
        log.debug("Получение всех режиссеров");
        return jdbcTemplate.query(SELECT_ALL_DIRECTORS, mapRowToDirector());
    }

    @Override
    public Director create(Director director) {
        log.debug("Создание режиссера: {}", director);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(CREATE_DIRECTOR, new String[]{"id"});

            stmt.setString(1, director.getName());

            return stmt;
        }, keyHolder);


        return director.toBuilder().id(keyHolder.getKey().longValue()).build();
    }

    @Override
    public Director update(Director director) {
        log.debug("Обновление режиссера: {}", director);
        getById(director.getId());

        jdbcTemplate.update(UPDATE_DIRECTOR, director.getName(), director.getId());

        return director;
    }

    public void deleteById(long id) {
        log.debug("Удаление режиссера с id: {}", id);
        getById(id);

        jdbcTemplate.update(DELETE_DIRECTOR, id);
    }

    public void delete(Director director) {
        deleteById(director.getId());
    }

    private static RowMapper<Director> mapRowToDirector() {
        return (rs, rowNum) ->
                Director.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .build();
    }
}
