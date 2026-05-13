package ru.senla.scooterrental.fleet.exceptions;

import ru.senla.scooterrental.common.exception.ConflictException;

public class ScooterUnavailableException
        extends ConflictException {

    public ScooterUnavailableException(String message) {
        super(message);
    }
}