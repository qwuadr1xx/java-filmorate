package ru.yandex.practicum.filmorate.storage.review;

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
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.feed.DbFeedStorage;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
public class DbReviewStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    private final FeedStorage dbFeedStorage;

    private static final String CREATE_REVIEW = "INSERT INTO REVIEWS (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_REVIEW_BY_ID = "SELECT * FROM REVIEWS WHERE review_id = ?";
    private static final String SELECT_ALL_REVIEWS = "SELECT * FROM REVIEWS ORDER BY useful DESC LIMIT ?";
    private static final String SELECT_REVIEWS_BY_FILM_ID = "SELECT * FROM REVIEWS WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
    private static final String UPDATE_REVIEW = "UPDATE REVIEWS SET content = ?, is_positive = ? WHERE review_id = ?";
    private static final String DELETE_REVIEW_BY_ID = "DELETE FROM REVIEWS WHERE review_id = ?";
    private static final String LIKE_REVIEW = "INSERT INTO REVIEWS_LIKES (review_id, user_id, is_like) VALUES (?, ?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM REVIEWS_LIKES WHERE review_id = ? AND user_id = ? ";
    private static final String DELETE_DISLIKE = "DELETE FROM REVIEWS_LIKES WHERE review_id = ? AND user_id = ? ";
    private static final String U_UPDATE_DECREASE_SQL_QUERY = "UPDATE REVIEWS SET useful = useful - 1 WHERE review_id = ?";
    private static final String U_UPDATE_INCREASE_SQL_QUERY = "UPDATE REVIEWS SET useful = useful + 1 WHERE review_id = ?";

    @Autowired
    public DbReviewStorage(JdbcTemplate jdbcTemplate, DbFeedStorage dbFeedStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.dbFeedStorage = dbFeedStorage;
    }

    @Override
    public Review createReview(Review review) {
        log.info("Выполняется запрос: {}", CREATE_REVIEW);
        checkUserExists(review.getUserId());
        checkFilmExists(review.getFilmId());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE_REVIEW, new String[]{"review_id"});
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive() != null ? review.getIsPositive() : false);
            ps.setInt(3, review.getUserId());
            ps.setInt(4, review.getFilmId());
            ps.setInt(5, review.getUseful());
            return ps;
        }, keyHolder);

        review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        dbFeedStorage.setRecord(FeedRecord.builder()
                .timestamp(Instant.now().toEpochMilli())
                .userId((long) review.getUserId())
                .eventType(EventType.REVIEW)
                .operation(Operation.ADD)
                .entityId((long) review.getReviewId())
                .build());
        return review;
    }

    @Override
    public Review getReviewById(Integer reviewId) {
        log.info("Выполняется запрос: {}", SELECT_REVIEW_BY_ID);
        try {
            return jdbcTemplate.queryForObject(SELECT_REVIEW_BY_ID, new Object[]{reviewId}, new ReviewRowMapper());
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(reviewId, Entity.REVIEW);
        }
    }

    @Override
    public List<Review> getReview(Integer filmId, Integer count) {
        int limit = Optional.ofNullable(count).orElse(10);

        if (filmId == null) {
            log.info("Выполняется запрос: {}", SELECT_ALL_REVIEWS);

            return jdbcTemplate.query(SELECT_ALL_REVIEWS, new Object[]{limit}, new ReviewRowMapper());
        } else {
            log.info("Выполняется запрос: {}", SELECT_REVIEWS_BY_FILM_ID);

            return jdbcTemplate.query(SELECT_REVIEWS_BY_FILM_ID, new Object[]{filmId, limit}, new ReviewRowMapper());
        }
    }

    @Override
    public Review updateReview(Review review) {
        log.info("Выполняется запрос: {}", UPDATE_REVIEW);
        getReviewById(review.getReviewId());

        jdbcTemplate.update(UPDATE_REVIEW, review.getContent(), review.getIsPositive(), review.getReviewId());

        dbFeedStorage.setRecord(FeedRecord.builder()
                .timestamp(Instant.now().toEpochMilli())
                .userId((long) review.getUserId())
                .eventType(EventType.REVIEW)
                .operation(Operation.UPDATE)
                .entityId((long) review.getReviewId())
                .build());
        return review;
    }

    @Override
    public void deleteReviewById(Integer reviewId) {
        log.info("Выполняется запрос: {}", DELETE_REVIEW_BY_ID);

        try {
            getReviewById(reviewId);
            dbFeedStorage.setRecord(FeedRecord.builder()
                    .timestamp(Instant.now().toEpochMilli())
                    .userId(jdbcTemplate.queryForObject("SELECT user_id FROM reviews WHERE review_id = ?", long.class, reviewId))
                    .eventType(EventType.REVIEW)
                    .operation(Operation.REMOVE)
                    .entityId((long) reviewId)
                    .build());
        } finally {
            jdbcTemplate.update(DELETE_REVIEW_BY_ID, reviewId);
        }
    }

    @Override
    public void likeReview(Integer reviewId, Integer userId) {
        log.info("Выполняется запрос: {}", LIKE_REVIEW);
        getReviewById(reviewId);
        checkUserExists(userId);
        deleteDislikeReview(reviewId, userId);

        jdbcTemplate.update(LIKE_REVIEW, reviewId, userId, true);
        jdbcTemplate.update(U_UPDATE_INCREASE_SQL_QUERY, reviewId);
    }

    @Override
    public void dislikeReview(Integer reviewId, Integer userId) {
        log.info("Выполняется запрос: {}", LIKE_REVIEW);
        getReviewById(reviewId);
        checkUserExists(userId);
        deleteLikeReview(reviewId, userId);

        jdbcTemplate.update(LIKE_REVIEW, reviewId, userId, false);
        jdbcTemplate.update(U_UPDATE_DECREASE_SQL_QUERY, reviewId);
    }

    @Override
    public void deleteLikeReview(Integer reviewId, Integer userId) {
        log.info("Выполняется запрос: {}", DELETE_LIKE);
        getReviewById(reviewId);
        checkUserExists(userId);

        int update = jdbcTemplate.update(DELETE_LIKE, reviewId, userId);
        if (update == 1) {
            jdbcTemplate.update(U_UPDATE_DECREASE_SQL_QUERY, reviewId);
        }
    }

    @Override
    public void deleteDislikeReview(Integer reviewId, Integer userId) {
        log.info("Выполняется запрос: {}", DELETE_DISLIKE);
        getReviewById(reviewId);
        checkUserExists(userId);

        int update = jdbcTemplate.update(DELETE_DISLIKE, reviewId, userId);
        if (update == 1) {
            jdbcTemplate.update(U_UPDATE_INCREASE_SQL_QUERY, reviewId);
        }
    }

    private void checkUserExists(Integer userId) {
        try {
            jdbcTemplate.queryForObject("SELECT 1 FROM users WHERE id = ?", Integer.class, userId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(userId, Entity.USER);
        }
    }

    private void checkFilmExists(Integer filmId) {
        try {
            jdbcTemplate.queryForObject("SELECT 1 FROM films WHERE id = ?", Integer.class, filmId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(filmId, Entity.FILM);
        }
    }

    private static class ReviewRowMapper implements RowMapper<Review> {
        @Override
        public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Review.builder()
                    .reviewId(rs.getInt("review_id"))
                    .content(rs.getString("content"))
                    .isPositive(rs.getBoolean("is_positive"))
                    .userId(rs.getInt("user_id"))
                    .filmId(rs.getInt("film_id"))
                    .useful(rs.getInt("useful"))
                    .build();
        }
    }
}


