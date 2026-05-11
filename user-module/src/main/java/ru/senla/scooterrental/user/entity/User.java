package ru.senla.scooterrental.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import ru.senla.scooterrental.user.enums.Role;
import ru.senla.scooterrental.user.enums.UserStatus;
import ru.senla.scooterrental.user.exceptions.InsufficientBalanceException;
import ru.senla.scooterrental.user.exceptions.UserValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Entity
@Table(name = "users")
public class User {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9]{10,15}$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "first_name", nullable = false, length = 255)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 255)
    private String lastName;

    @Column(nullable = false, unique = true, length = 50)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balance;

    @Column(name = "subscription_purchased_at")
    private LocalDateTime subscriptionPurchasedAt;

    @Column(name = "subscription_expires_at")
    private LocalDateTime subscriptionExpiresAt;

    @Column(nullable = false)
    private boolean verified;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected User() { }

    public User(String email,
                String password,
                String firstName,
                String lastName,
                String phone) {
        this.status = UserStatus.ACTIVE;
        this.balance = BigDecimal.ZERO;
        this.verified = false;

        setEmail(email);
        setInitialPassword(password);
        setFirstName(firstName);
        setLastName(lastName);
        setPhone(phone);
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        String normalizedEmail = normalizeRequiredText(email, "Email");

        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new UserValidationException(
                    "Некорректный формат email."
            );
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
        assignRole(Role.ADMIN);
    }

    public UserStatus getStatus() {
        return status;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public LocalDateTime getSubscriptionPurchasedAt() {
        return subscriptionPurchasedAt;
    }

    public LocalDateTime getSubscriptionExpiresAt() {
        return subscriptionExpiresAt;
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

    public boolean hasActiveSubscription() {
        return subscriptionExpiresAt != null
                && subscriptionExpiresAt.isAfter(LocalDateTime.now());
    }

    public void buyThreeDaySubscription(BigDecimal price) {
        validateMoneyAmount(price, "Стоимость абонемента");
        subtractBalance(price);

        this.subscriptionPurchasedAt = LocalDateTime.now();
        this.subscriptionExpiresAt = this.subscriptionPurchasedAt.plusDays(3);
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