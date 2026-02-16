package com.banking.customer.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standardized error response structure for all API errors.
 * Used by GlobalExceptionHandler to provide consistent error feedback to
 * clients.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * The time when the error occurred.
     */
    private LocalDateTime timestamp;

    /**
     * HTTP Status code (e.g., 404, 400).
     */
    private int status;

    /**
     * HTTP Status error phrase (e.g., "Not Found", "Bad Request").
     */
    private String error;

    /**
     * Detailed error message explaining what went wrong.
     * Use this to show user-friendly messages.
     */
    private String message;

    /**
     * The API path where the error occurred (e.g., "/api/v1/customers").
     */
    private String path;
}