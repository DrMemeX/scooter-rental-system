package ru.senla.scooterrental.user.exceptions;

import ru.senla.scooterrental.common.exception.PaymentRequiredException;

public class InsufficientBalanceException
        extends PaymentRequiredException {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
