package ru.senla.scooterrental.user.exceptions;

import ru.senla.scooterrental.common.exception.ForbiddenException;

public class UserBlockedException
        extends ForbiddenException {

    public UserBlockedException(String message) {
        super(message);
    }
}
