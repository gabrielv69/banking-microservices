package com.banking.account.exception;

/**
 * Exception thrown when trying to perform a transaction with insufficient balance
 *
 */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}