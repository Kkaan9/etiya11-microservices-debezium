package com.etiya.productservice.services.exceptions;

/**
 * Thrown when a business rule is violated (e.g. a requested product does not exist).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
