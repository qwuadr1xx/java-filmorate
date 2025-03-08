package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;

@Slf4j
@Repository
public class DbUserStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    private static final String CREATE_USER = "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";

    private static final String SELECT_ALL_USERS = "SELECT * FROM users";

    private static final String SELECT_USER_BY_ID = "SELECT * FROM users WHERE id = ?";

    private static final String UPDATE_USER = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

    private static final String ADD_FRIEND = "INSERT INTO friendship(user_id, friend_id) VALUES (?, ?)";

    private static final String REMOVE_FRIEND = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";

    private static final String DELETE_USER = "DELETE FROM users WHERE id = ?";

    private static final String DELETE_USER_FROM_FRIENDSHIP = "DELETE FROM friendship WHERE user_id = ?";

    private static final String DELETE_LIKE_BY_USER = "DELETE FROM likes WHERE user_id = ?";

    private static final String DELETE_FRIEND_FROM_FRIENDSHIP = "DELETE FROM friendship WHERE friend_id = ?";

    private static final String GET_USERS_FRIENDS = "SELECT * FROM users AS u " +
            "WHERE u.id IN (SELECT friend_id FROM friendship WHERE user_id = ?)";

    private static final String GET_INTERSECTION_FRIENDS = "SELECT u.* FROM users AS u " +
            "JOIN friendship AS f1 ON u.id = f1.friend_id " +
            "JOIN friendship AS f2 ON u.id = f2.friend_id " +
            "WHERE f1.user_id = ? AND f2.user_id = ?";

    @Autowired
    public DbUserStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> getAll() {
        log.debug("Получение всех пользователей");
        List<User> users = jdbcTemplate.query(SELECT_ALL_USERS, mapRowToUser());
        log.info("Получено {} пользователей", users.size());
        return users;
    }

    @Override
    public User getById(long id) {
        log.debug("Получение пользователя с id: {}", id);
        User user = jdbcTemplate.queryForObject(SELECT_USER_BY_ID, mapRowToUser(), id);
        log.info("Получен пользователь: {}", user);
        return user;
    }

    @Override
    public User create(User user) {
        log.debug("Создание пользователя: {}", user);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(CREATE_USER, new String[]{"id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);

        User createdUser = user.toBuilder()
                .id(keyHolder.getKey().longValue())
                .build();
        log.info("Пользователь создан с id: {}", createdUser.getId());
        return createdUser;
    }

    @Override
    public User update(User user) {
        getById(user.getId());

        log.debug("Обновление пользователя: {}", user);
        jdbcTemplate.update(UPDATE_USER, user.getEmail(), user.getLogin(),
                user.getName(), user.getBirthday(), user.getId());
        log.info("Пользователь обновлен с id: {}", user.getId());
        return user;
    }

    @Override
    public User addFriend(long id, long friendId) {
        getById(id);
        User friend = getById(friendId);
        log.debug("Пользователь с id {} добавляет в друзья пользователя с id {}", id, friendId);

        jdbcTemplate.update(ADD_FRIEND, id, friendId);
        return friend;
    }

    @Override
    public User removeFriend(long id, long friendId) {
        getById(id);
        User friend = getById(friendId);
        log.debug("Пользователь с id {} удаляет из друзей пользователя с id {}", id, friendId);

        jdbcTemplate.update(REMOVE_FRIEND, id, friendId);
        return friend;
    }

    @Override
    public List<User> getFriends(long id) {
        getById(id);

        log.debug("Получение друзей для пользователя с id {}", id);
        List<User> friends = jdbcTemplate.query(GET_USERS_FRIENDS, mapRowToUser(), id);
        log.info("Получены {} друзья для пользователя с id {}", friends, id);
        return friends;
    }

    @Override
    public List<User> getIntersectionFriends(long id, long otherId) {
        getById(id);
        getById(otherId);

        log.debug("Получение общих друзей для пользователей с id {} и {}", id, otherId);
        List<User> commonFriends = jdbcTemplate.query(GET_INTERSECTION_FRIENDS, mapRowToUser(), id, otherId);
        log.info("Получены {} общие друзья для пользователей с id {} и {}", commonFriends, id, otherId);
        return commonFriends;
    }

    @Override
    public void deleteById(long id) {
        log.debug("Удаление пользователя с id: {}", id);

        jdbcTemplate.update(DELETE_LIKE_BY_USER, id);
        jdbcTemplate.update(DELETE_FRIEND_FROM_FRIENDSHIP, id);
        jdbcTemplate.update(DELETE_USER_FROM_FRIENDSHIP, id);
        jdbcTemplate.update(DELETE_USER, id);

        log.info("Пользователь {} удален", id);
    }

    private static RowMapper<User> mapRowToUser() {
        return (rs, rowNum) ->
                User.builder()
                        .id(rs.getLong("id"))
                        .email(rs.getString("email"))
                        .login(rs.getString("login"))
                        .name(rs.getString("name"))
                        .birthday(rs.getDate("birthday").toLocalDate())
                        .build();
    }
}
