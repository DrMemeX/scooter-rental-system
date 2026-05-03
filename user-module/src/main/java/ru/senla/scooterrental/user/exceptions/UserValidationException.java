package ru.senla.scooterrental.user.exceptions;

public class UserValidationException
        extends RuntimeException {

    public UserValidationException(String message) {
        super(message);
    }
}
