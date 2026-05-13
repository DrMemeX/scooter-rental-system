package ru.senla.scooterrental.fleet.exceptions;

import ru.senla.scooterrental.common.exception.ConflictException;

public class InvalidScooterStateException
        extends ConflictException {

    public InvalidScooterStateException(String message) {
        super(message);
    }
}