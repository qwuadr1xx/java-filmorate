package ru.yandex.practicum.filmorate.storage.feed;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Slf4j
@Repository
public class DbFeedStorage implements FeedStorage {
    JdbcTemplate jdbcTemplate;

    private static final String CREATE_FEED = "INSERT INTO feed(timestamp, user_id, event_type, operation, entity_id) VALUES (?, ?, ?, ?, ?)";

    private static final String GET_FEED_BY_ID = "SELECT * FROM feed WHERE user_id = ?";

    @Autowired
    public DbFeedStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<FeedRecord> getRecord(long id) {
        log.debug("Получение записи с id: {}", id);
        List<FeedRecord> feedRecordList = jdbcTemplate.query(GET_FEED_BY_ID, mapRowToFeedRecord(), id);

        if (feedRecordList.isEmpty()) {
            throw new NotFoundException(id, Entity.FEED);
        }

        log.info("Получение записи: {}", feedRecordList);
        return feedRecordList;
    }

    @Override
    public void setRecord(FeedRecord feedRecord) {
        log.debug("Создание записи: {}", feedRecord);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(CREATE_FEED, new String[]{"event_id"});
            stmt.setTimestamp(1, Timestamp.from(Instant.ofEpochMilli(feedRecord.getTimestamp())));
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
                .timestamp(rs.getTimestamp("timestamp").toInstant().toEpochMilli())
                .userId(rs.getLong("user_id"))
                .eventType(EventType.valueOf(rs.getString("event_type")))
                .operation(Operation.valueOf(rs.getString("operation")))
                .eventId(rs.getLong("event_id"))
                .entityId(rs.getLong("entity_id"))
                .build();
    }
}
