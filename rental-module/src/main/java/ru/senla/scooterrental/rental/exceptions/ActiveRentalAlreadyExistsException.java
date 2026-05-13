package ru.senla.scooterrental.rental.exceptions;

import ru.senla.scooterrental.common.exception.ConflictException;

public class ActiveRentalAlreadyExistsException
        extends ConflictException {

    public ActiveRentalAlreadyExistsException(String message) {
        super(message);
    }
}
