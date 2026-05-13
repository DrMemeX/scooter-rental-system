package ru.senla.scooterrental.fleet.exceptions;

import ru.senla.scooterrental.common.exception.NotFoundException;

public class FleetEntityNotFoundException
        extends NotFoundException {

    public FleetEntityNotFoundException(String message) {
        super(message);
    }
}