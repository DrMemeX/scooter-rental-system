package ru.senla.scooterrental.maintenance.exceptions;

import ru.senla.scooterrental.common.exception.ValidationException;

public class MaintenanceValidationException extends ValidationException {

    public MaintenanceValidationException(String message) {
        super(message);
    }
}