package ru.senla.scooterrental.rental.exceptions;

public class RentalNotFoundException extends RuntimeException {

    public RentalNotFoundException(String message) {
        super(message);
    }
}
