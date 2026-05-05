package ru.senla.scooterrental.rental.exceptions;

public class ActiveRentalAlreadyExistsException
        extends RuntimeException {

    public ActiveRentalAlreadyExistsException(String message) {
        super(message);
    }
}
