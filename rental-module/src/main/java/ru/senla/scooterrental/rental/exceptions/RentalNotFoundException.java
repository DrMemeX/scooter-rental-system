package ru.senla.scooterrental.rental.exceptions;

import ru.senla.scooterrental.common.exception.NotFoundException;

public class RentalNotFoundException extends NotFoundException {

    public RentalNotFoundException(String message) {
        super(message);
    }
}
