package ru.senla.scooterrental.user.exceptions;

import ru.senla.scooterrental.common.exception.ValidationException;

public class UserValidationException
        extends ValidationException {

    public UserValidationException(String message) {
        super(message);
    }
}
