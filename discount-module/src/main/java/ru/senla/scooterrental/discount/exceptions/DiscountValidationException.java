package ru.senla.scooterrental.discount.exceptions;

import ru.senla.scooterrental.common.exception.ValidationException;

public class DiscountValidationException extends ValidationException {

  public DiscountValidationException(String message) {
    super(message);
  }
}