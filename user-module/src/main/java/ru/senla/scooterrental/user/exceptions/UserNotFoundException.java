package ru.senla.scooterrental.user.exceptions;

import ru.senla.scooterrental.common.exception.NotFoundException;

public class UserNotFoundException
        extends NotFoundException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
