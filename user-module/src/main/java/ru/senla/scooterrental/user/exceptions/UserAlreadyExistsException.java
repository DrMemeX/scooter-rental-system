package ru.senla.scooterrental.user.exceptions;

import ru.senla.scooterrental.common.exception.ConflictException;

public class UserAlreadyExistsException
        extends ConflictException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
