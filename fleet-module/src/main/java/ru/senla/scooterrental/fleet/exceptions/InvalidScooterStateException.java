package ru.senla.scooterrental.fleet.exceptions;

public class InvalidScooterStateException
        extends RuntimeException {

    public InvalidScooterStateException(String message) {
        super(message);
    }
}