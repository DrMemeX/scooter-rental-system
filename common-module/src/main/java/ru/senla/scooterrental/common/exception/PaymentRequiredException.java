package ru.senla.scooterrental.common.exception;

public class PaymentRequiredException extends ApplicationException {

    public PaymentRequiredException(String message) {
        super(message);
    }
}