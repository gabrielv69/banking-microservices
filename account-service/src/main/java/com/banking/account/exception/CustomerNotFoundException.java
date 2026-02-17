package com.banking.account.exception;

/**
 * Exception thrown when a customer is not found in customer-service.
 *
 * Context: account-service needs to validate customer existence
 * by calling customer-service API. If customer doesn't exist there,
 * this exception is thrown.
 *
 * Note: This is NOT the same as customer-service's CustomerNotFoundException.
 * Each microservice has its own bounded context and exception hierarchy.
 *
 * HTTP Status: 404 Not Found
 */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String message) {
        super(message);
    }

    public CustomerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}