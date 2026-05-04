package ru.senla.scooterrental.rental.exceptions;

public class InvalidRentalStateException
        extends RuntimeException {

    public InvalidRentalStateException(String message) {
        super(message);
    }
}
