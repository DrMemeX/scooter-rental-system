package ru.senla.scooterrental.user.dto;

import ru.senla.scooterrental.user.enums.Role;
import ru.senla.scooterrental.user.enums.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserResponse {

    private final Long id;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final String phone;

    private final Role role;
    private final UserStatus status;

    private final BigDecimal balance;
    private final boolean verified;

    private final LocalDateTime createdAt;

    public UserResponse(Long id,
                        String email,
                        String firstName,
                        String lastName,
                        String phone,
                        Role role,
                        UserStatus status,
                        BigDecimal balance,
                        boolean verified,
                        LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.balance = balance;
        this.verified = verified;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public Role getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public boolean isVerified() {
        return verified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}