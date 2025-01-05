package ru.yandex.practicum.filmorate.exception;

public class ValidationException extends Exception {
    ValidationException(String message) {
        super(message);
    }
}
