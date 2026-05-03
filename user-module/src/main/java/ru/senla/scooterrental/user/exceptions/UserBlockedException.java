package ru.senla.scooterrental.user.exceptions;

public class UserBlockedException
        extends RuntimeException {

    public UserBlockedException(String message) {
        super(message);
    }
}
