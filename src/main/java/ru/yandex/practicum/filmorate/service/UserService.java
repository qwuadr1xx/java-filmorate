package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserMapper;
import ru.yandex.practicum.filmorate.dto.UserRequest;
import ru.yandex.practicum.filmorate.enums.Entity;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.model.FeedRecord;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.DbUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage dbUserStorage;
    private final FilmStorage filmStorage;
    private final FeedStorage dbFeedStorage; // Добавляем FeedStorage

    @Autowired
    public UserService(DbUserStorage dbUserStorage, FilmStorage filmStorage, FeedStorage dbFeedStorage) {
        this.dbUserStorage = dbUserStorage;
        this.filmStorage = filmStorage;
        this.dbFeedStorage = dbFeedStorage; // Инициализация FeedStorage
    }

    public List<Film> getUsersRecommendations(long userId) {
        validateId(userId);
        List<Long> userFilms = filmStorage.getFilmsUserById(userId);
        List<Long> recommendedFilmIds = filmStorage.getUsersRecommendations(userId);
        recommendedFilmIds.removeAll(userFilms);
        return filmStorage.getFilmsByIds(recommendedFilmIds);
    }

    public List<User> getUsers() {
        return dbUserStorage.getAll();
    }

    public User getUserById(long id) {
        validateId(id);

        return dbUserStorage.getById(id);
    }

    public List<FeedRecord> getFeedRecord(long id) {
        validateId(id);

        return dbFeedStorage.getRecord(id);
    }

    public User createUser(UserRequest userRequest) {
        User user = UserMapper.mapUserFromDto(userRequest);

        return dbUserStorage.create(user);
    }

    public User updateUser(UserRequest userRequest) {
        User user = UserMapper.mapUserFromDto(userRequest);

        return dbUserStorage.update(user);
    }

    public User addFriend(long id, long friendId) {
        validateId(id);
        validateId(friendId);

        return dbUserStorage.addFriend(id, friendId);
    }

    public void deleteUser(long id) {
        validateId(id);

        dbUserStorage.deleteById(id);
    }

    public User removeFriend(long id, long friendId) {
        return dbUserStorage.removeFriend(id, friendId);
    }

    public List<User> getFriends(long id) {
        return dbUserStorage.getFriends(id);
    }

    public List<User> getIntersectionFriends(long id, long otherId) {
        return dbUserStorage.getIntersectionFriends(id, otherId);
    }

    private static void validateId(long id) {
        if (id < 0) {
            throw new BadRequestException("id не может быть отрицательным", Entity.USER);
        } else if (id == 0) {
            throw new BadRequestException("id не может быть равным нулю", Entity.USER);
        }
    }
}
