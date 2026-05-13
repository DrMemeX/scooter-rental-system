package ru.senla.scooterrental.rental.exceptions;

import ru.senla.scooterrental.common.exception.ConflictException;

public class InvalidRentalStateException
        extends ConflictException {

    public InvalidRentalStateException(String message) {
        super(message);
    }
}
