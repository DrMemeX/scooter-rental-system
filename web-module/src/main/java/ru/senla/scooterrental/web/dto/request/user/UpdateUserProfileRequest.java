package ru.senla.scooterrental.web.dto.request.user;

public record UpdateUserProfileRequest(

        String firstName,

        String lastName,

        String phone

) {
}