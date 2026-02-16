package com.banking.account.client;

import com.banking.account.constants.AccountMessages;
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
     * Returns empty Mono on error (graceful degradation).
     *
     * @param customerId customer identifier
     * @return Mono of CustomerResponse (empty if not found or error)
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
                            return Mono.error(new RuntimeException(
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
                })
                .onErrorResume(error -> {
                    log.warn(AccountMessages.LOG_CLIENT_FALLBACK, error.getMessage());
                    return Mono.empty();
                });
    }
}
