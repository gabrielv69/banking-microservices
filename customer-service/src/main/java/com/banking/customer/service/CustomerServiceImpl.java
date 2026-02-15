package com.banking.customer.service;

import com.banking.customer.exception.CustomerAlreadyExistsException;
import com.banking.customer.exception.CustomerNotFoundException;
import com.banking.customer.model.Customer;
import com.banking.customer.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service implementation for Customer business logic
 * Implements logging, validation, and error handling
 */
@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    // Constructor injection as required
    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<Customer> getAllCustomers() {
        log.info("Fetching all customers");
        return customerRepository.findAll()
                .doOnNext(customer -> log.debug("Found customer: {}", customer.getName()))
                .doOnComplete(() -> log.info("Successfully fetched all customers"));
    }

    @Override
    public Mono<Customer> getCustomerById(Long customerId) {
        log.info("Fetching customer with ID: {}", customerId);
        return customerRepository.findById(customerId)
                .doOnNext(customer -> log.debug("Found customer: {}", customer.getName()))
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(
                        "Customer not found with ID: " + customerId)))
                .doOnError(error -> log.error("Error fetching customer with ID {}: {}",
                        customerId, error.getMessage()));
    }

    @Override
    public Mono<Customer> createCustomer(Customer customer) {
        log.info("Creating new customer with identification: {}", customer.getIdentification());

        return customerRepository.existsByIdentification(customer.getIdentification())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        log.warn("Customer already exists with identification: {}",
                                customer.getIdentification());
                        return Mono.error(new CustomerAlreadyExistsException(
                                "Customer already exists with identification: " +
                                        customer.getIdentification()));
                    }

                    // Set default active status if not provided
                    if (customer.getStatus() == null) {
                        customer.setStatus(true);
                    }

                    return customerRepository.save(customer)
                            .doOnSuccess(saved -> log.info("Customer created successfully with ID: {}",
                                    saved.getId()))
                            .doOnError(error -> log.error("Error creating customer: {}",
                                    error.getMessage()));
                });
    }

    @Override
    public Mono<Customer> updateCustomer(Long customerId, Customer customer) {
        log.info("Updating customer with ID: {}", customerId);

        return customerRepository.findById(customerId)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(
                        "Customer not found with ID: " + customerId)))
                .flatMap(existingCustomer -> {
                    // Update fields
                    existingCustomer.setName(customer.getName());
                    existingCustomer.setGender(customer.getGender());
                    existingCustomer.setIdentification(customer.getIdentification());
                    existingCustomer.setAddress(customer.getAddress());
                    existingCustomer.setPhone(customer.getPhone());
                    existingCustomer.setPassword(customer.getPassword());
                    existingCustomer.setStatus(customer.getStatus());

                    return customerRepository.save(existingCustomer)
                            .doOnSuccess(updated -> log.info("Customer updated successfully with ID: {}",
                                    updated.getId()))
                            .doOnError(error -> log.error("Error updating customer with ID {}: {}",
                                    customerId, error.getMessage()));
                });
    }

    @Override
    public Mono<Void> deleteCustomer(Long customerId) {
        log.info("Deleting customer with ID: {}", customerId);

        return customerRepository.findById(customerId)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(
                        "Customer not found with ID: " + customerId)))
                .flatMap(customer -> customerRepository.delete(customer)
                        .doOnSuccess(v -> log.info("Customer deleted successfully with ID: {}",
                                customerId))
                        .doOnError(error -> log.error("Error deleting customer with ID {}: {}",
                                customerId, error.getMessage())));
    }

}