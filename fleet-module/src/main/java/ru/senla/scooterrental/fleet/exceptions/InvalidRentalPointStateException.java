package ru.senla.scooterrental.fleet.exceptions;

import ru.senla.scooterrental.common.exception.ConflictException;

public class InvalidRentalPointStateException
        extends ConflictException {

    public InvalidRentalPointStateException(String message) {
        super(message);
    }
}