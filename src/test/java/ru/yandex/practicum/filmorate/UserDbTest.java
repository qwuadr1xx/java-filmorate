package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.DbFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.DbUserStorage;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({DbUserStorage.class, DbFilmStorage.class})
class UserDbTest {

    @Autowired
    private DbUserStorage userStorage;

    @Test
    void testCreateAndFindUser() {
        User user = User.builder()
                .email("test@example.com")
                .login("testlogin")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User createdUser = userStorage.create(user);
        User foundUser = userStorage.getById(createdUser.getId());
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(createdUser.getId());
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testUpdateUser() {
        User user = User.builder()
                .email("update@example.com")
                .login("updatelogin")
                .name("Update User")
                .birthday(LocalDate.of(1985, 5, 15))
                .build();
        User createdUser = userStorage.create(user);
        User updatedUser = createdUser.toBuilder()
                .email("updated@example.com")
                .name("Updated Name")
                .build();
        userStorage.update(updatedUser);
        User foundUser = userStorage.getById(createdUser.getId());
        assertThat(foundUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(foundUser.getName()).isEqualTo("Updated Name");
    }
}
