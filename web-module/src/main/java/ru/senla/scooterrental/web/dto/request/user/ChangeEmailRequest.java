package ru.senla.scooterrental.web.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ChangeEmailRequest(

        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Некорректный формат email")
        String newEmail

) {
}