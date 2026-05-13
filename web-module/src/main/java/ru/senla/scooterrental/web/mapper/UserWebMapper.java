package ru.senla.scooterrental.web.mapper;

import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.web.dto.request.user.RegisterUserRequest;
import ru.senla.scooterrental.web.dto.response.user.UserResponse;

public final class UserWebMapper {

    private UserWebMapper() {
    }

    public static User toEntity(RegisterUserRequest request) {
        return new User(
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName(),
                request.phone()
        );
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                user.getBalance(),
                user.getSubscriptionPurchasedAt(),
                user.getSubscriptionExpiresAt(),
                user.isVerified(),
                user.getCreatedAt()
        );
    }
}