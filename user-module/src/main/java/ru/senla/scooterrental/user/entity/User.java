package ru.senla.scooterrental.user.entity;

import ru.senla.scooterrental.user.enums.Role;
import ru.senla.scooterrental.user.enums.UserStatus;
import ru.senla.scooterrental.user.exceptions.InsufficientBalanceException;
import ru.senla.scooterrental.user.exceptions.UserValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class User {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9]{10,15}$");

    private Long id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;

    private Role role;
    private UserStatus status;
    private BigDecimal balance;
    private boolean verified;
    private final LocalDateTime createdAt;

    public User(String email,
                String password,
                String firstName,
                String lastName,
                String phone) {
        this.status = UserStatus.ACTIVE;
        this.balance = BigDecimal.ZERO;
        this.verified = false;
        this.createdAt = LocalDateTime.now();

        setEmail(email);
        setInitialPassword(password);
        setFirstName(firstName);
        setLastName(lastName);
        setPhone(phone);
    }

    public Long getId() {
        return id;
    }

    public void assignId(Long id) {
        if (this.id != null) {
            throw new UserValidationException(
                    "ID пользователя уже назначен и не может быть изменён."
            );
        }

        if (id == null || id <= 0) {
            throw new UserValidationException(
                    "ID пользователя должен быть положительным числом."
            );
        }

        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        String normalizedEmail = normalizeRequiredText(email, "Email");

        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new UserValidationException("Некорректный формат email.");
        }

        this.email = normalizedEmail.toLowerCase();
    }

    public String getPassword() {
        return password;
    }

    private void setInitialPassword(String password) {
        this.password = validatePassword(password);
    }

    public void changePassword(String newPassword) {
        this.password = validatePassword(newPassword);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        String normalizedFirstName = normalizeRequiredText(firstName, "Имя");

        if (normalizedFirstName.length() < 2) {
            throw new UserValidationException(
                    "Имя должно содержать не менее 2 символов."
            );
        }

        this.firstName = normalizedFirstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        String normalizedLastName = normalizeRequiredText(lastName, "Фамилия");

        if (normalizedLastName.length() < 2) {
            throw new UserValidationException(
                    "Фамилия должна содержать не менее 2 символов."
            );
        }

        this.lastName = normalizedLastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        String normalizedPhone = normalizeRequiredText(phone, "Телефон")
                .replace(" ", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "");

        if (!PHONE_PATTERN.matcher(normalizedPhone).matches()) {
            throw new UserValidationException(
                    "Некорректный формат телефона. Используйте только цифры и, при необходимости, знак '+'."
            );
        }

        this.phone = normalizedPhone;
    }

    public Role getRole() {
        return role;
    }

    public void assignUserRole() {
        assignRole(Role.USER);
    }

    public void assignManagerRole() {
        assignRole(Role.MANAGER);
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

    public void verify() {
        this.verified = true;
    }

    public void block() {
        this.status = UserStatus.BLOCKED;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public boolean isBlocked() {
        return status == UserStatus.BLOCKED;
    }

    public void addBalance(BigDecimal amount) {
        validateMoneyAmount(amount, "Сумма пополнения");
        ensureBalanceInitialized();
        this.balance = this.balance.add(amount);
    }

    public void subtractBalance(BigDecimal amount) {
        validateMoneyAmount(amount, "Сумма списания");
        ensureBalanceInitialized();

        if (balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Недостаточно средств на балансе пользователя."
            );
        }

        this.balance = this.balance.subtract(amount);
    }

    private void assignRole(Role role) {
        if (this.role != null) {
            throw new UserValidationException(
                    "Роль пользователя уже назначена и не может быть изменена."
            );
        }

        if (role == null) {
            throw new UserValidationException(
                    "Роль пользователя не может быть пустой."
            );
        }

        this.role = role;
    }

    private static String normalizeRequiredText(String value, String fieldName) {
        if (value == null) {
            throw new UserValidationException(
                    fieldName + " не может быть пустым."
            );
        }

        String normalizedValue = value.trim();

        if (normalizedValue.isEmpty()) {
            throw new UserValidationException(
                    fieldName + " не может быть пустым."
            );
        }

        return normalizedValue;
    }

    private static void validateMoneyAmount(BigDecimal amount, String fieldName) {
        if (amount == null) {
            throw new UserValidationException(
                    fieldName + " не может быть пустой."
            );
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new UserValidationException(
                    fieldName + " должна быть больше нуля."
            );
        }
    }

    private String validatePassword(String password) {
        String normalizedPassword = normalizeRequiredText(password, "Пароль");

        if (normalizedPassword.length() < 8) {
            throw new UserValidationException(
                    "Пароль должен содержать не менее 8 символов."
            );
        }

        if (!normalizedPassword.matches(".*\\d.*")) {
            throw new UserValidationException(
                    "Пароль должен содержать хотя бы одну цифру."
            );
        }

        if (!normalizedPassword.matches(".*[A-Za-zА-Яа-я].*")) {
            throw new UserValidationException(
                    "Пароль должен содержать хотя бы одну букву."
            );
        }

        return normalizedPassword;
    }

    private void ensureBalanceInitialized() {
        if (balance == null) {
            throw new UserValidationException(
                    "Баланс пользователя не инициализирован."
            );
        }
    }
}