package ru.senla.scooterrental.web.dto.response.user;

import ru.senla.scooterrental.user.enums.Role;
import ru.senla.scooterrental.user.enums.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserResponse(

        Long id,

        String email,

        String firstName,

        String lastName,

        String phone,

        Role role,

        UserStatus status,

        BigDecimal balance,

        LocalDateTime subscriptionPurchasedAt,

        LocalDateTime subscriptionExpiresAt,

        boolean verified,

        LocalDateTime createdAt

) {
}