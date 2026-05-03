package ru.senla.scooterrental.user.mapper;

import ru.senla.scooterrental.user.dto.UserCreateRequest;
import ru.senla.scooterrental.user.dto.UserResponse;
import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.user.exceptions.UserValidationException;

public final class UserMapper {

    private UserMapper() {
    }

    public static User toEntity(UserCreateRequest request) {
        if (request == null) {
            throw new UserValidationException(
                    "Запрос на создание пользователя не может быть пустым."
            );
        }

        return new User(
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                request.getPhone()
        );
    }

    public static UserResponse toResponse(User user) {
        if (user == null) {
            throw new UserValidationException(
                    "Пользователь не может быть пустым."
            );
        }

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                user.getBalance(),
                user.isVerified(),
                user.getCreatedAt()
        );
    }
}