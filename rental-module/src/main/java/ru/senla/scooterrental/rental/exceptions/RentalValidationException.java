package ru.senla.scooterrental.rental.exceptions;

public class RentalValidationException
        extends RuntimeException {

    public RentalValidationException(String message) {
        super(message);
    }
}
