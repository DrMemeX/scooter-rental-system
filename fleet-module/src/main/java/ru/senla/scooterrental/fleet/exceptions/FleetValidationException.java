package ru.senla.scooterrental.fleet.exceptions;

public class FleetValidationException
        extends RuntimeException {

    public FleetValidationException(String message) {
        super(message);
    }
}
