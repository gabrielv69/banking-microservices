package com.banking.account.exception;

/**
 * Exception thrown when report date range is invalid.
 * Examples:
 * - startDate after endDate
 * - endDate in the future
 * - Date range too large
 *
 * HTTP Status: 400 Bad Request
 */
public class InvalidDateRangeException extends RuntimeException {

    public InvalidDateRangeException(String message) {
        super(message);
    }

    public InvalidDateRangeException(String message, Throwable cause) {
        super(message, cause);
    }
}
