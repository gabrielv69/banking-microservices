package com.banking.account.exception;

/**
 * Exception thrown when a movement is not found
 */
public class MovementNotFoundException extends RuntimeException {

    public MovementNotFoundException(String message) {
        super(message);
    }
}
