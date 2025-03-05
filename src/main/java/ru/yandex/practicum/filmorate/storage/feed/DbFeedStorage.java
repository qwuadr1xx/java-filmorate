package ru.yandex.practicum.filmorate.storage.feed;

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
import ru.yandex.practicum.filmorate.model.FeedRecord;

import java.sql.PreparedStatement;

@Slf4j
@Repository
public class DbFeedStorage implements FeedStorage {
    JdbcTemplate jdbcTemplate;

    private static final String CREATE_FEED = "INSERT INTO feed(timestamp, user_id, event_type, operation, entity_id) VALUES (?, ?, ?, ?, ?)";

    @Autowired
    public DbFeedStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public FeedRecord getRecord(long id) {
        log.debug("Получение записи с id: {}", id);
        FeedRecord feedRecord;

        try {
            feedRecord = jdbcTemplate.queryForObject("SELECT 1 FROM feed WHERE user_id = ?", mapRowToFeedRecord(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(id, Entity.FEED);
        }

        log.info("Получение записи: {}", feedRecord);
        return feedRecord;
    }

    @Override
    public void setRecord(FeedRecord feedRecord) {
        log.debug("Создание записи: {}", feedRecord);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(CREATE_FEED, new String[]{"eventId"});
            stmt.setTimestamp(1, feedRecord.getTimestamp());
            stmt.setLong(2, feedRecord.getUserId());
            stmt.setString(3, feedRecord.getEventType().toString());
            stmt.setString(4, feedRecord.getOperation().toString());
            stmt.setLong(5, feedRecord.getEntityId());
            return stmt;
        }, keyHolder);

        FeedRecord createdFilm = feedRecord.toBuilder()
                .eventId(keyHolder.getKey().longValue())
                .build();
        log.info("Запись создана: {}", createdFilm);
    }

    private static RowMapper<FeedRecord> mapRowToFeedRecord() {
        return (rs, rowNum) -> FeedRecord.builder()
                .timestamp(rs.getTimestamp("timestamp"))
                .userId(rs.getLong("user_id"))
                .eventType(EventType.valueOf(rs.getString("event_type")))
                .operation(Operation.valueOf(rs.getString("operation")))
                .eventId(rs.getLong("event_id"))
                .entityId(rs.getLong("entity_id"))
                .build();
    }
}
