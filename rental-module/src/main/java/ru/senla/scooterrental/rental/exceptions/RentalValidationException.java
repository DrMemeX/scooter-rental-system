package ru.senla.scooterrental.rental.exceptions;

import ru.senla.scooterrental.common.exception.ValidationException;

public class RentalValidationException
        extends ValidationException {

    public RentalValidationException(String message) {
        super(message);
    }
}
