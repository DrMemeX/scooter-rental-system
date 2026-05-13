package ru.senla.scooterrental.maintenance.exceptions;

public class ServiceEventNotFoundException extends RuntimeException {

    public ServiceEventNotFoundException(String message) {
        super(message);
    }
}