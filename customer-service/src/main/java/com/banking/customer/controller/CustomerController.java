package com.banking.customer.controller;

import com.banking.customer.constants.CustomerMessages;
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
 * REST Controller for Customer management.
 * Implements the CustomersApi interface generated from OpenAPI specification.
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

    @Override
    public Mono<ResponseEntity<CustomerResponse>> createCustomer(Mono<CustomerRequest> customerRequest,
                                                                 ServerWebExchange exchange) {
        log.info(CustomerMessages.LOG_CREATE);
        return customerRequest
                .map(customerMapper::toEntity)
                .flatMap(customerService::createCustomer)
                .map(customerMapper::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCustomer(Long customerId, ServerWebExchange exchange) {
        log.info(CustomerMessages.LOG_DELETE, customerId);

        return customerService.deleteCustomer(customerId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @Override
    public Mono<ResponseEntity<Flux<CustomerResponse>>> getAllCustomers(ServerWebExchange exchange) {
        log.info(CustomerMessages.LOG_GET_ALL);

        Flux<CustomerResponse> customers = customerService.getAllCustomers()
                .map(customerMapper::toResponse);

        return Mono.just(ResponseEntity.ok(customers));
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> getCustomerById(Long customerId, ServerWebExchange exchange) {
        log.info(CustomerMessages.LOG_GET_BY_ID, customerId);

        return customerService.getCustomerById(customerId)
                .map(customerMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> updateCustomer(Long customerId,
                                                                 Mono<CustomerRequest> customerRequest, ServerWebExchange exchange) {
        log.info(CustomerMessages.LOG_UPDATE, customerId);

        return customerRequest
                .map(customerMapper::toEntity)
                .flatMap(customer -> customerService.updateCustomer(customerId, customer))
                .map(customerMapper::toResponse)
                .map(ResponseEntity::ok);
    }
}
