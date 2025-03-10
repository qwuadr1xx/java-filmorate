package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.DbReviewStorage;

import java.util.List;

@Service
public class ReviewService {

    private final DbReviewStorage dbReviewStorage;

    @Autowired
    public ReviewService(DbReviewStorage dbReviewStorage) {
        this.dbReviewStorage = dbReviewStorage;
    }

    public Review createReview(Review review) {
        return dbReviewStorage.createReview(review);
    }

    public Review getReviewById(Integer reviewId) {
        return dbReviewStorage.getReviewById(reviewId);
    }

    public List<Review> getReview(Integer filmId, Integer count) {
        return dbReviewStorage.getReview(filmId, count);
    }

    public Review updateReview(Review review) {
        return dbReviewStorage.updateReview(review);
    }

    public void deleteReviewById(Integer reviewId) {
        dbReviewStorage.deleteReviewById(reviewId);
    }

    public void likeReview(Integer reviewId, Integer userId) {
        dbReviewStorage.likeReview(reviewId, userId);
    }

    public void dislikeReview(Integer reviewId, Integer userId) {
        dbReviewStorage.dislikeReview(reviewId, userId);
    }

    public void deleteLikeReview(Integer reviewId, Integer userId) {
        dbReviewStorage.deleteLikeReview(reviewId, userId);
    }

    public void deleteDislikeReview(Integer reviewId, Integer userId) {
        dbReviewStorage.deleteDislikeReview(reviewId, userId);
    }
}
