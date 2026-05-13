package ru.senla.scooterrental.web.dto.request.auth;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @Email(message = "Некорректный формат email")
        @NotBlank(message = "Email не может быть пустым")
        String email,

        @NotBlank(message = "Пароль не может быть пустым")
        String password
) {
}