package com.banking.account.client;

import com.banking.account.infrastructure.adapter.rest.generated.model.CustomerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Client for communicating with customer-service microservice
 *
 * Responsibility:
 * - Encapsulates all HTTP calls to customer-service
 * - Handles errors and provides meaningful error messages
 * - Provides reactive API (returns Mono/Flux)
 */
@Slf4j
@Component
public class CustomerServiceClient {
    private final WebClient customerServiceWebClient;

    /**
     * Constructor injection
     *
     * @param customerServiceWebClient WebClient configured for customer-service
     */
    public CustomerServiceClient(WebClient customerServiceWebClient) {
        this.customerServiceWebClient = customerServiceWebClient;
    }


    /**
     * Get customer information by ID from customer-service
     *
     * Flow:
     * 1. Build URI with customer ID
     * 2. Make HTTP GET request
     * 3. Deserialize JSON response to CustomerResponse
     * 4. Handle errors (404, 500, network issues)
     * 5. Return Mono<CustomerResponse>
     *
     * Error Handling:
     * - 404 NOT FOUND → Customer doesn't exist
     * - 500 INTERNAL SERVER ERROR → customer-service is down or has error
     * - Network error → Connection refused, timeout
     *
     * @param customerId customer identifier
     * @return Mono of CustomerResponse (empty if not found or error)
     */
    public Mono<CustomerResponse> getCustomerById(Long customerId) {
        log.info("Calling customer-service: GET /customers/{}", customerId);

        return customerServiceWebClient
                // Step 1: Build the request
                .get()  // HTTP GET method
                .uri("/customers/{id}", customerId)  // URI with path variable

                // Step 2: Execute and retrieve response
                .retrieve()  // Execute the request

                // Step 3: Handle HTTP errors
                .onStatus(
                        status -> status.is4xxClientError(),  // 400-499 errors
                        clientResponse -> {
                            log.error("Customer not found or bad request: customerId={}", customerId);
                            return Mono.error(new RuntimeException(
                                    "Customer not found with ID: " + customerId));
                        }
                )
                .onStatus(
                        status -> status.is5xxServerError(),  // 500-599 errors
                        clientResponse -> {
                            log.error("Customer-service error: customerId={}", customerId);
                            return Mono.error(new RuntimeException(
                                    "Customer-service is unavailable"));
                        }
                )

                // Step 4: Deserialize JSON to CustomerResponse
                .bodyToMono(CustomerResponse.class)

                // Step 5: Log success
                .doOnSuccess(customer -> {
                    if (customer != null) {
                        log.info("Successfully retrieved customer: id={}, name={}",
                                customerId, customer.getName());
                    }
                })

                // Step 6: Log errors
                .doOnError(error ->
                        log.error("Error calling customer-service for customerId {}: {}",
                                customerId, error.getMessage())
                )

                // Step 7: Fallback on error (optional - return empty instead of error)
                .onErrorResume(error -> {
                    log.warn("Returning empty result due to error: {}", error.getMessage());
                    return Mono.empty();  // Return empty instead of propagating error
                });
    }

    /**
     * Alternative: Get customer with error propagation (no fallback)
     *
     * Use this version if you want errors to bubble up to the caller
     *
     * @param customerId customer identifier
     * @return Mono of CustomerResponse
     */
    public Mono<CustomerResponse> getCustomerByIdStrict(Long customerId) {
        log.info("Calling customer-service (strict): GET /customers/{}", customerId);

        return customerServiceWebClient
                .get()
                .uri("/customers/{id}", customerId)
                .retrieve()
                .bodyToMono(CustomerResponse.class)
                .doOnSuccess(customer ->
                        log.info("Retrieved customer: {}", customer.getName()))
                .doOnError(error ->
                        log.error("Error retrieving customer {}: {}", customerId, error.getMessage()));
        // No onErrorResume - errors propagate to caller
    }

}
