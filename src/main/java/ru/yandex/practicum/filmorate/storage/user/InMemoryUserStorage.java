package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.enums.Entity;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users;
    private final Map<Long, Set<Long>> friends;
    private long nextId;

    @Autowired
    public InMemoryUserStorage() {
        users = new HashMap<>();
        friends = new HashMap<>();
        nextId = 1;
    }

    @Override
    public User create(User user) {
        long localId = generateId();
        User localUser = user.toBuilder().id(localId).build();

        users.put(localId, localUser);
        return localUser;
    }

    @Override
    public User update(User user) {
        Long localId = user.getId();

        if (localId == null) {
            throw new BadRequestException("значение id равно null", Entity.USER);
        }
        isIdExists(localId);

        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> getById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User addFriend(long id, long friendId) {
        isIdExists(id);
        isIdExists(friendId);

        Set<Long> userFriends;
        if (friends.containsKey(id)) {
            userFriends = friends.get(id);
        } else {
            userFriends = new HashSet<>();
        }

        Set<Long> friendFriends;
        if (friends.containsKey(friendId)) {
            friendFriends = friends.get(friendId);
        } else {
            friendFriends = new HashSet<>();
        }

        userFriends.add(friendId);
        friendFriends.add(id);
        friends.put(id, userFriends);
        friends.put(friendId, friendFriends);

        return users.get(friendId);
    }

    @Override
    public User removeFriend(long id, long friendId) {
        isIdExists(id);
        isIdExists(friendId);

        Set<Long> userFriends;
        if (friends.containsKey(id)) {
            userFriends = friends.get(id);
        } else {
            throw new BadRequestException(String.format("У пользователя с id %d нет друзей", id), Entity.USER);
        }

        Set<Long> friendFriends;
        if (friends.containsKey(friendId)) {
            friendFriends = friends.get(friendId);
        } else {
            throw new BadRequestException(String.format("У пользователя с id %d нет друзей", friendId), Entity.USER);
        }

        userFriends.remove(friendId);
        friendFriends.remove(id);
        friends.put(id, userFriends);
        friends.put(friendId, friendFriends);

        return users.get(friendId);
    }

    @Override
    public List<User> getFriends(long id) {
        isIdExists(id);

        return new ArrayList<>(friends.get(id).stream()
                .map(users::get)
                .toList());
    }

    @Override
    public List<User> getIntersectionFriends(long id, long otherId) {
        isIdExists(id);
        isIdExists(otherId);

        Set<Long> userFriends;
        if (friends.containsKey(id)) {
            userFriends = friends.get(id);
        } else {
            throw new BadRequestException(String.format("У пользователя с id %d нет друзей", id), Entity.USER);
        }

        Set<Long> otherUserFriends;
        if (friends.containsKey(otherId)) {
            otherUserFriends = friends.get(otherId);
        } else {
            throw new BadRequestException(String.format("У пользователя с id %d нет друзей", otherId), Entity.USER);
        }

        Set<User> intersection = new HashSet<>(userFriends.stream().map(users::get).toList());
        intersection.retainAll(otherUserFriends.stream().map(users::get).toList());
        return new ArrayList<>(intersection);
    }

    @Override
    public void isIdExists(long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException(id, Entity.USER);
        }
    }

    private long generateId() {
        return nextId++;
    }
}
