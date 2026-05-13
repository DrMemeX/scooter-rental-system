package ru.senla.scooterrental.maintenance.exceptions;

import ru.senla.scooterrental.common.exception.NotFoundException;

public class ServiceEventNotFoundException extends NotFoundException {

    public ServiceEventNotFoundException(String message) {
        super(message);
    }
}