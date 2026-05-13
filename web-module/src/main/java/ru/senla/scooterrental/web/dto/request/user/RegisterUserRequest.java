package ru.senla.scooterrental.web.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(

        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Некорректный формат email")
        String email,

        @NotBlank(message = "Пароль не может быть пустым")
        @Size(min = 8, message = "Пароль должен содержать не менее 8 символов")
        String password,

        @NotBlank(message = "Имя не может быть пустым")
        String firstName,

        @NotBlank(message = "Фамилия не может быть пустой")
        String lastName,

        @NotBlank(message = "Телефон не может быть пустым")
        String phone

) {
}