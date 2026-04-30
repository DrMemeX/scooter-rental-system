package ru.senla.scooterrental.fleet.exceptions;

public class ScooterUnavailableException extends RuntimeException {

    public ScooterUnavailableException(String message) {
        super(message);
    }
}