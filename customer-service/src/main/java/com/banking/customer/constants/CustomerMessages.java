package com.banking.customer.constants;

/**
 * Centralized message constants for customer-service.
 * Provides consistent, reusable messages for logging, exceptions, and API
 * responses.
 */
public final class CustomerMessages {

    private CustomerMessages() {
        // Utility class — prevent instantiation
    }

    // === Exception Messages ===
    public static final String CUSTOMER_NOT_FOUND = "Customer not found with ID: %d";
    public static final String CUSTOMER_ALREADY_EXISTS = "Customer already exists with identification: %s";

    // === Validation Messages ===
    public static final String VALIDATION_ERROR = "Validation error";
    public static final String INVALID_REQUEST_BODY = "Invalid request body. Please check field types.";

    // === Error Messages ===
    public static final String DATABASE_ERROR = "Database error";
    public static final String DUPLICATE_KEY_ERROR = "Data integrity violation: Duplicate key. Please check unique fields.";
    public static final String UNEXPECTED_ERROR = "An unexpected error occurred: %s";

    // === Log Messages ===
    public static final String LOG_GET_ALL = "GET /api/v1/customers - Getting all customers";
    public static final String LOG_GET_BY_ID = "GET /api/v1/customers/{} - Getting customer by ID";
    public static final String LOG_CREATE = "POST /api/v1/customers - Creating new customer";
    public static final String LOG_UPDATE = "PUT /api/v1/customers/{} - Updating customer";
    public static final String LOG_DELETE = "DELETE /api/v1/customers/{} - Deleting customer";
    public static final String LOG_CREATED_SUCCESS = "Customer created successfully with ID: {}";
    public static final String LOG_UPDATED_SUCCESS = "Customer updated successfully with ID: {}";
    public static final String LOG_DELETED_SUCCESS = "Customer deleted successfully with ID: {}";
    public static final String LOG_FOUND = "Customer found with ID: {}";
    public static final String LOG_ERROR_CREATE = "Error creating customer: {}";
    public static final String LOG_ERROR_UPDATE = "Error updating customer: {}";
    public static final String LOG_ERROR_DELETE = "Error deleting customer: {}";
    public static final String LOG_ERROR_GET = "Error getting customer: {}";
    public static final String LOG_CREATING_CUSTOMER = "Creating new customer with identification: {}";
    public static final String LOG_UPDATING_CUSTOMER = "Updating customer with ID: {}";
    public static final String LOG_DELETING_CUSTOMER = "Deleting customer with ID: {}";
    public static final String LOG_GETTING_ALL = "Getting all customers";
    public static final String LOG_GETTING_ALL_SUCCESS = "Successfully fetched all customers";
    public static final String LOG_GETTING_BY_ID = "Getting customer with ID: {}";
}
