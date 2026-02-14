package com.banking.customer.controller;

import com.banking.customer.infrastructure.adapter.rest.generated.CustomersApi;
import com.banking.customer.infrastructure.adapter.rest.generated.model.CustomerRequest;
import com.banking.customer.infrastructure.adapter.rest.generated.model.CustomerResponse;
import com.banking.customer.mapper.CustomerMapper;
import com.banking.customer.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST Controller for Customer management
 * Implements the CustomersApi interface generated from OpenAPI specification
 * Uses constructor injection and reactive programming with WebFlux
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class CustomerController implements CustomersApi {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    // Constructor injection as required
    public CustomerController(CustomerService customerService,
                              CustomerMapper customerMapper) {
        this.customerService = customerService;
        this.customerMapper = customerMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ResponseEntity<CustomerResponse>> createCustomer(Mono<CustomerRequest> customerRequest, ServerWebExchange exchange) {
        log.info("POST /api/v1/customers - Creating new customer");

        return customerRequest
                .map(customerMapper::toEntity)
                .flatMap(customerService::createCustomer)
                .map(customerMapper::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .doOnSuccess(response ->
                        log.info("Customer created successfully with ID: {}",
                                response.getBody().getCustomerId()))
                .doOnError(error ->
                        log.error("Error creating customer: {}", error.getMessage()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ResponseEntity<Void>> deleteCustomer(Long customerId, ServerWebExchange exchange) {
        log.info("DELETE /api/v1/customers/{} - Deleting customer", customerId);

        return customerService.deleteCustomer(customerId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .doOnSuccess(response ->
                        log.info("Customer deleted successfully with ID: {}", customerId))
                .doOnError(error ->
                        log.error("Error deleting customer {}: {}", customerId, error.getMessage()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ResponseEntity<Flux<CustomerResponse>>> getAllCustomers(ServerWebExchange exchange) {
        log.info("GET /api/v1/customers - Fetching all customers");
        Flux<CustomerResponse> customers = customerService.getAllCustomers()
                .map(customerMapper::toResponse)
                .doOnComplete(() -> log.info("Successfully retrieved all customers"));
        return Mono.just(ResponseEntity.ok(customers));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ResponseEntity<CustomerResponse>> getCustomerById(Long customerId, ServerWebExchange exchange) {
        log.info("GET /api/v1/customers/{} - Fetching customer", customerId);

        return customerService.getCustomerById(customerId)
                .map(customerMapper::toResponse)
                .map(ResponseEntity::ok)
                .doOnSuccess(response ->
                        log.info("Customer found with ID: {}", customerId))
                .doOnError(error ->
                        log.error("Error fetching customer {}: {}", customerId, error.getMessage()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ResponseEntity<CustomerResponse>> updateCustomer(Long customerId, Mono<CustomerRequest> customerRequest, ServerWebExchange exchange) {

        log.info("PUT /api/v1/customers/{} - Updating customer", customerId);

        return customerRequest
                .map(customerMapper::toEntity)
                .flatMap(customer -> customerService.updateCustomer(customerId, customer))
                .map(customerMapper::toResponse)
                .map(ResponseEntity::ok)
                .doOnSuccess(response ->
                        log.info("Customer updated successfully with ID: {}", customerId))
                .doOnError(error ->
                        log.error("Error updating customer {}: {}", customerId, error.getMessage()));
    }
}

