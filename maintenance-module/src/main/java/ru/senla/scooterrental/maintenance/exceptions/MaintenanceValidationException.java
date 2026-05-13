package ru.senla.scooterrental.maintenance.exceptions;

public class MaintenanceValidationException extends RuntimeException {

    public MaintenanceValidationException(String message) {
        super(message);
    }
}