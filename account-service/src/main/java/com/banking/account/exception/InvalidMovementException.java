package com.banking.account.exception;


/**
 * Exception thrown when movement data is invalid
 * Example: negative value, invalid movement type, etc.
 */
public class InvalidMovementException extends RuntimeException {

    public InvalidMovementException(String message) {
        super(message);
    }
}
