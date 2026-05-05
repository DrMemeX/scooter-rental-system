package ru.senla.scooterrental.discount.exceptions;

public class DiscountValidationException extends RuntimeException {

  public DiscountValidationException(String message) {
    super(message);
  }
}