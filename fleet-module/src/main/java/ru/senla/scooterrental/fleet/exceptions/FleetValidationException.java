package ru.senla.scooterrental.fleet.exceptions;

import ru.senla.scooterrental.common.exception.ValidationException;

public class FleetValidationException
        extends ValidationException {

    public FleetValidationException(String message) {
        super(message);
    }
}
