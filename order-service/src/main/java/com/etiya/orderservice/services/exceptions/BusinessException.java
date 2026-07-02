package com.etiya.orderservice.services.exceptions;

/**
 * Thrown when a business rule is violated (e.g. a requested order does not exist).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
