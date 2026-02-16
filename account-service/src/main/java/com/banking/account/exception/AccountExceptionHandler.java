package com.banking.account.exception;

import com.banking.account.constants.AccountMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import java.time.LocalDateTime;

/**
 * Global exception handler for all REST controllers in account-service.
 * Centralizes error handling using @RestControllerAdvice.
 */
@Slf4j
@RestControllerAdvice
public class AccountExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFoundException(
            AccountNotFoundException ex, ServerWebExchange exchange) {
        log.error("Account not found: {}", ex.getMessage());
        return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage(), exchange);
    }

    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAccountAlreadyExistsException(
            AccountAlreadyExistsException ex, ServerWebExchange exchange) {
        log.error("Account already exists: {}", ex.getMessage());
        return buildResponseEntity(HttpStatus.CONFLICT, ex.getMessage(), exchange);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalanceException(
            InsufficientBalanceException ex, ServerWebExchange exchange) {
        log.error("Insufficient balance: {}", ex.getMessage());
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), exchange);
    }

    @ExceptionHandler(InvalidMovementException.class)
    public ResponseEntity<ErrorResponse> handleInvalidMovementException(
            InvalidMovementException ex, ServerWebExchange exchange) {
        log.error("Invalid movement: {}", ex.getMessage());
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), exchange);
    }

    @ExceptionHandler(MovementNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMovementNotFoundException(
            MovementNotFoundException ex, ServerWebExchange exchange) {
        log.error("Movement not found: {}", ex.getMessage());
        return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage(), exchange);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            WebExchangeBindException ex, ServerWebExchange exchange) {
        log.error("Validation error: {}", ex.getMessage());

        String message = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof org.springframework.validation.FieldError fieldError) {
                        return fieldError.getField() + ": " + error.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                .orElse(AccountMessages.VALIDATION_ERROR);

        return buildResponseEntity(HttpStatus.BAD_REQUEST, message, exchange);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, ServerWebExchange exchange) {
        log.error("Data integrity violation: {}", ex.getMessage());

        String message = (ex.getMessage() != null && ex.getMessage().contains("duplicate key"))
                ? AccountMessages.DUPLICATE_KEY_ERROR
                : AccountMessages.DATABASE_ERROR;

        return buildResponseEntity(HttpStatus.CONFLICT, message, exchange);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ErrorResponse> handleServerWebInputException(
            ServerWebInputException ex, ServerWebExchange exchange) {
        log.error("Invalid input: {}", ex.getMessage());
        return buildResponseEntity(HttpStatus.BAD_REQUEST, AccountMessages.INVALID_REQUEST_BODY, exchange);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, ServerWebExchange exchange) {
        log.error("Unexpected error: ", ex);
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR,
                String.format(AccountMessages.UNEXPECTED_ERROR, ex.getMessage()), exchange);
    }

    /**
     * Handle CustomerNotFoundException (from customer-service client)
     * Returns 404 Not Found
     */
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFoundException(
            CustomerNotFoundException ex,
            ServerWebExchange exchange) {

        log.error("Customer not found: {}", ex.getMessage());
        return buildResponseEntity(HttpStatus.NOT_FOUND,
                String.format(AccountMessages.UNEXPECTED_ERROR, ex.getMessage()), exchange);
    }


    /**
     * Builds a standardized error ResponseEntity.
     * Eliminates repetitive ErrorResponse construction across handlers.
     */
    private ResponseEntity<ErrorResponse> buildResponseEntity(
            HttpStatus status, String message, ServerWebExchange exchange) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(exchange.getRequest().getPath().value())
                .build();
        return ResponseEntity.status(status).body(errorResponse);
    }
}
