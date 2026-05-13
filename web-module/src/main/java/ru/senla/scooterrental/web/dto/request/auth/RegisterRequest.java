package ru.senla.scooterrental.web.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @Email(message = "Некорректный формат email")
        @NotBlank(message = "Email не может быть пустым")
        String email,

        @NotBlank(message = "Пароль не может быть пустым")
        @Size(min = 8, message = "Пароль должен содержать не менее 8 символов")
        String password,

        @NotBlank(message = "Имя не может быть пустым")
        String firstName,

        @NotBlank(message = "Фамилия не может быть пустой")
        String lastName,

        @NotBlank(message = "Телефон не может быть пустым")
        @Pattern(
                regexp = "^\\+?[0-9\\s\\-()]{10,20}$",
                message = "Некорректный формат телефона"
        )
        String phone
) {
}