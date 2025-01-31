package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserMapper;
import ru.yandex.practicum.filmorate.dto.UserRequest;
import ru.yandex.practicum.filmorate.enums.Entity;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage inMemoryUserStorage;

    public List<User> getUsers() {
        return new ArrayList<>(inMemoryUserStorage.getAll());
    }

    public User getUserById(long id) {
        return inMemoryUserStorage.getById(id).orElseThrow(() -> new NotFoundException(id, Entity.USER));
    }

    public User createUser(UserRequest userRequest) {
        User user = UserMapper.mapUserFromDto(userRequest);

        return inMemoryUserStorage.create(user);
    }

    public User updateUser(UserRequest userRequest) {
        User user = UserMapper.mapUserFromDto(userRequest);

        return inMemoryUserStorage.update(user);
    }

    public User addFriend(long id, long friendId) {
        return inMemoryUserStorage.addFriend(id, friendId);
    }

    public User removeFriend(long id, long friendId) {
        return inMemoryUserStorage.removeFriend(id, friendId);
    }

    public List<User> getFriends(long id) {
        return inMemoryUserStorage.getFriends(id);
    }

    public List<User> getIntersectionFriends(long id, long otherId) {
        return inMemoryUserStorage.getIntersectionFriends(id, otherId);
    }

    public void isUserIdExists(long id) {
        inMemoryUserStorage.isIdExists(id);
    }
}
