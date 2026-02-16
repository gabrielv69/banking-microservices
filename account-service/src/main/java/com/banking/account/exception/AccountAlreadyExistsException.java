package com.banking.account.exception;

/**
 * Exception thrown when trying to create an account that already exists
 */
public class AccountAlreadyExistsException extends RuntimeException {

    public AccountAlreadyExistsException(String message) {
        super(message);
    }
}
