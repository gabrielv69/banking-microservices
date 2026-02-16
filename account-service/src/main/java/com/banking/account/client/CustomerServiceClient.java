package com.banking.account.client;

import com.banking.account.constants.AccountMessages;
import com.banking.account.exception.CustomerNotFoundException;
import com.banking.account.infrastructure.adapter.rest.generated.model.CustomerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Client for communicating with customer-service microservice.
 * Encapsulates all HTTP calls to customer-service and provides
 * reactive API with error handling.
 */
@Slf4j
@Component
public class CustomerServiceClient {

    private final WebClient customerServiceWebClient;

    public CustomerServiceClient(WebClient customerServiceWebClient) {
        this.customerServiceWebClient = customerServiceWebClient;
    }

    /**
     * Get customer information by ID from customer-service.
     * Throws CustomerNotFoundException if customer not found (404).
     *
     * This method follows Fail Fast principle:
     * - 404 Client Error → CustomerNotFoundException (propagated)
     * - 5xx Server Error → RuntimeException (can be handled by caller)
     *
     * @param customerId customer identifier
     * @return Mono of CustomerResponse
     * @throws CustomerNotFoundException if customer not found (404)
     */
    public Mono<CustomerResponse> getCustomerById(Long customerId) {
        log.info(AccountMessages.LOG_CLIENT_CALLING, customerId);

        return customerServiceWebClient
                .get()
                .uri("/customers/{id}", customerId)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(),
                        clientResponse -> {
                            log.error(AccountMessages.LOG_CLIENT_ERROR_NOT_FOUND, customerId);
                            // Throw specific exception that will be propagated
                            return Mono.error(new CustomerNotFoundException(
                                    String.format(AccountMessages.CUSTOMER_NOT_FOUND, customerId)));
                        })
                .onStatus(
                        status -> status.is5xxServerError(),
                        clientResponse -> {
                            log.error(AccountMessages.LOG_CLIENT_ERROR_SERVER, customerId);
                            return Mono.error(new RuntimeException(
                                    AccountMessages.CUSTOMER_SERVICE_UNAVAILABLE));
                        })
                .bodyToMono(CustomerResponse.class)
                .doOnSuccess(customer -> {
                    if (customer != null) {
                        log.info(AccountMessages.LOG_CLIENT_SUCCESS,
                                customerId, customer.getName());
                    }
                });
    }
}
