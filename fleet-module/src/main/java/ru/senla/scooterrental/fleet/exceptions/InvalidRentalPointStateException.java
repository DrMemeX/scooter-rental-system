package ru.senla.scooterrental.fleet.exceptions;

public class InvalidRentalPointStateException
        extends RuntimeException {

    public InvalidRentalPointStateException(String message) {
        super(message);
    }
}