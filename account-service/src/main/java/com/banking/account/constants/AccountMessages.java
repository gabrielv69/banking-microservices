package com.banking.account.constants;

/**
 * Centralized message constants for account-service.
 * Provides consistent, reusable messages for logging, exceptions, and API
 * responses.
 */
public final class AccountMessages {

    private AccountMessages() {
        // Utility class — prevent instantiation
    }

    // === Account Exception Messages ===
    public static final String ACCOUNT_NOT_FOUND = "Account not found with ID: %d";
    public static final String ACCOUNT_NOT_FOUND_NUMBER = "Account not found with number: %s";
    public static final String ACCOUNT_ALREADY_EXISTS = "Account already exists with number: %s";

    // === Movement Exception Messages ===
    public static final String MOVEMENT_NOT_FOUND = "Movement not found with ID: %d";
    public static final String INSUFFICIENT_BALANCE = "Saldo no disponible";
    public static final String MOVEMENT_VALUE_MUST_BE_POSITIVE = "El valor del movimiento debe ser mayor que cero";
    public static final String INVALID_MOVEMENT_TYPE = "Invalid movement type: %s. Must be CREDIT or DEBIT";

    // === Validation Messages ===
    public static final String VALIDATION_ERROR = "Validation error";
    public static final String INVALID_REQUEST_BODY = "Invalid request body. Please check field types.";

    // === Error Messages ===
    public static final String DATABASE_ERROR = "Database error";
    public static final String DUPLICATE_KEY_ERROR = "Data integrity violation: Duplicate key. Please check unique fields.";
    public static final String UNEXPECTED_ERROR = "An unexpected error occurred: %s";

    // === Customer Client Messages ===
    public static final String CUSTOMER_NOT_FOUND = "Customer not found with ID: %d";
    public static final String CUSTOMER_SERVICE_ERROR = "Error communicating with customer-service: %s";
    public static final String CUSTOMER_SERVICE_UNAVAILABLE = "Customer service is not available";

    // === Account Log Messages ===
    public static final String LOG_ACCOUNT_GET_ALL = "GET /api/v1/accounts - Getting all accounts";
    public static final String LOG_ACCOUNT_GET_ALL_SUCCESS = "Successfully fetched all accounts";
    public static final String LOG_ACCOUNT_GET_BY_ID = "GET /api/v1/accounts/{} - Getting account by ID";
    public static final String LOG_ACCOUNT_CREATE = "POST /api/v1/accounts - Creating new account";
    public static final String LOG_ACCOUNT_UPDATE = "PUT /api/v1/accounts/{} - Updating account";
    public static final String LOG_ACCOUNT_DELETE = "DELETE /api/v1/accounts/{} - Deleting account";
    public static final String LOG_ACCOUNT_CREATED = "Account created successfully with ID: {}";
    public static final String LOG_ACCOUNT_UPDATED = "Account updated successfully with ID: {}";
    public static final String LOG_ACCOUNT_DELETED = "Account deleted successfully with ID: {}";
    public static final String LOG_ACCOUNT_ERROR_UPDATE = "Error updating account with ID {}: {}";
    public static final String LOG_ACCOUNT_ERROR_DELETE = "Error deleting account with ID {}: {}";

    // === Movement Log Messages ===
    public static final String LOG_MOVEMENT_GET_ALL = "GET /api/v1/movements - Getting all movements";
    public static final String LOG_MOVEMENT_GET_ALL_SUCCESS = "Successfully fetched all movements";
    public static final String LOG_MOVEMENT_GET_BY_ID = "GET /api/v1/movements/{} - Getting movement by ID";
    public static final String LOG_MOVEMENT_DELETE = "DELETE /api/v1/movements/{} - Deleting movement";
    public static final String LOG_MOVEMENT_REGISTERED = "Movement registered: type={}, value={}, newBalance={}";
    public static final String LOG_MOVEMENT_DELETED_SUCCESS = "Movement deleted successfully with ID: {}";
    public static final String LOG_MOVEMENT_ERROR = "Error registering movement: {}";
    // === Report Log Messages ===
    public static final String LOG_REPORT_GENERATE = "Generating account statement for customer: {}, from: {} to: {}";
    public static final String LOG_REPORT_GENERATED = "Account statement generated for customer: {}";
    public static final String LOG_REPORT_GENERATED_SUCCESS = "Account statement generated successfully for customer: {}";
    public static final String LOG_REPORT_ERROR = "Error generating account statement: {}";

    // === Service Layer Log Messages ===
    public static final String LOG_CREATING_ACCOUNT = "Creating account with number: {}";
    public static final String LOG_UPDATING_ACCOUNT = "Updating account with ID: {}";
    public static final String LOG_DELETING_ACCOUNT = "Deleting account with ID: {}";
    public static final String LOG_REGISTERING_MOVEMENT = "Registering {} movement of {} for account {}";
    public static final String LOG_BALANCE_UPDATED = "Balance updated for account {}: {}";
    public static final String LOG_ACCOUNT_GET_BY_NUMBER = "Fetching account with number: {}";
    public static final String LOG_ACCOUNT_GET_BY_CUSTOMER = "Fetching accounts for customer ID: {}";
    public static final String LOG_MOVEMENT_GET_BY_ACCOUNT = "Fetching movements for account ID: {}";
    public static final String LOG_MOVEMENT_GET_BY_DATE_RANGE = "Fetching movements for account {} between {} and {}";
    public static final String LOG_MOVEMENT_CREATE = "POST /api/v1/movements - Creating new movement";
    public static final String LOG_MOVEMENT_DELETE_WARN = "DELETE /api/v1/movements/{} - Consider reversal instead";

    // === Report Detail Log Messages ===
    public static final String LOG_REPORT_BUILDING_DETAIL = "Building statement detail for account: {}";
    public static final String LOG_REPORT_MOVEMENTS_COUNT = "Account {} has {} movements in period";
    public static final String LOG_REPORT_GENERATED_DETAIL = "Account statement generated successfully - CustomerId: {}, Accounts: {}";
    public static final String LOG_REPORT_ERROR_DETAIL = "Error generating account statement for customer {}: {}";

    // === Customer Client Log Messages ===
    public static final String LOG_CLIENT_CALLING = "Calling customer-service: GET /customers/{}";
    public static final String LOG_CLIENT_SUCCESS = "Successfully retrieved customer: id={}, name={}";
    public static final String LOG_CLIENT_ERROR_NOT_FOUND = "Customer not found or bad request: customerId={}";
    public static final String LOG_CLIENT_ERROR_SERVER = "Customer-service error: customerId={}";
    public static final String LOG_CLIENT_ERROR_CALL = "Error calling customer-service for customerId {}: {}";
    public static final String LOG_CLIENT_FALLBACK = "Returning empty result due to error: {}";
    public static final String LOG_CLIENT_STRICT_CALLING = "Calling customer-service (strict): GET /customers/{}";
    public static final String LOG_CLIENT_STRICT_SUCCESS = "Retrieved customer: {}";
    public static final String LOG_CLIENT_STRICT_ERROR = "Error retrieving customer {}: {}";
}
