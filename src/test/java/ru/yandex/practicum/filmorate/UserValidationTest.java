package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.dto.UserRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest {
    private UserRequest user;
    private static Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validateUserWithoutLogin() {
        user = UserRequest.builder()
                .id(1L)
                .email("user@example.com")
                .login("")
                .birthday(LocalDate.of(2000, 10, 10))
                .build();
        Set<ConstraintViolation<UserRequest>> violations = validator.validate(user);
        assertEquals(2, violations.size());
    }

    @Test
    void validateUserWithoutEmail() {
        user = UserRequest.builder()
                .id(1L)
                .email("")
                .login("User1")
                .birthday(LocalDate.of(2000, 10, 10))
                .build();
        Set<ConstraintViolation<UserRequest>> violations = validator.validate(user);
        assertEquals(1, violations.size());
        assertEquals("Электронная почта не может быть пустой", violations.iterator().next().getMessage());
    }

    @Test
    void validateUserWithIncorrectEmail() {
        user = UserRequest.builder()
                .id(1L)
                .email("incorrect email")
                .login("User1")
                .birthday(LocalDate.of(2000, 10, 10))
                .build();
        Set<ConstraintViolation<UserRequest>> violations = validator.validate(user);
        assertEquals(1, violations.size());
        assertEquals("email не соответствует стандарту",
                violations.iterator().next().getMessage());
    }

    @Test
    void validateUserWithIncorrectLogin() {
        user = UserRequest.builder()
                .id(1L)
                .email("user@example.com")
                .login("us er")
                .birthday(LocalDate.of(2000, 10, 10))
                .build();
        Set<ConstraintViolation<UserRequest>> violations = validator.validate(user);
        assertEquals(1, violations.size());
        assertEquals("Логин содержит недопустимые символы",
                violations.iterator().next().getMessage());
    }

    @Test
    void validateUserWithFutureBirthday() {
        user = UserRequest.builder()
                .id(1L)
                .email("user@example.com")
                .login("User1")
                .birthday(LocalDate.of(2050, 10, 10))
                .build();
        Set<ConstraintViolation<UserRequest>> violations = validator.validate(user);
        assertEquals(1, violations.size());
        assertEquals("Дата рождения не может быть в будущем",
                violations.iterator().next().getMessage());
    }

    @Test
    void validateGoodUser() {
        user = UserRequest.builder()
                .id(1L)
                .email("user@example.com")
                .login("User1")
                .birthday(LocalDate.of(2000, 10, 10))
                .build();
        Set<ConstraintViolation<UserRequest>> violations = validator.validate(user);
        assertEquals(0, violations.size());
    }
}