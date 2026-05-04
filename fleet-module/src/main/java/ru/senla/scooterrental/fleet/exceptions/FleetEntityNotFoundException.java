package ru.senla.scooterrental.fleet.exceptions;

public class FleetEntityNotFoundException
        extends RuntimeException {

    public FleetEntityNotFoundException(String message) {
        super(message);
    }
}