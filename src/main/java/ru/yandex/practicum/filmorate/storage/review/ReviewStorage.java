package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {

    Review createReview(Review review);

    Review getReviewById(Integer reviewId);

    List<Review> getReview(Integer filmId, Integer count);

    Review updateReview(Review review);

    void deleteReviewById(Integer reviewId);

    void likeReview(Integer reviewId, Integer userId);

    void dislikeReview(Integer reviewId, Integer userId);

    void deleteLikeReview(Integer reviewId, Integer userId);

    void deleteDislikeReview(Integer reviewId, Integer userId);
}
