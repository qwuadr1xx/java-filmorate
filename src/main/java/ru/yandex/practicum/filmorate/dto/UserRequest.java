package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder(toBuilder = true)
@Data
public class UserRequest {
    private final Long id;

    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "email не соответствует стандарту")
    private final String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Логин содержит недопустимые символы")
    private final String login;

    private final String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private final LocalDate birthday;
}
