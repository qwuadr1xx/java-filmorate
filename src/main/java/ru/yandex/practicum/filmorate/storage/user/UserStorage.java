package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.List;

public interface UserStorage extends Storage<User> {
    User addFriend(long id, long friendId);

    User removeFriend(long id, long friendId);

    List<User> getFriends(long id);

    List<User> getIntersectionFriends(long id, long otherId);
}
