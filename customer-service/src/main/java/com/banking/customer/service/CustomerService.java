package com.banking.customer.service;

import com.banking.customer.model.Customer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service interface for Customer business logic
 */
public interface CustomerService {

    /**
     * Get all customers
     * @return Flux of all customers
     */
    Flux<Customer> getAllCustomers();

    /**
     * Get customer by ID
     * @param customerId customer ID
     * @return Mono of Customer
     */
    Mono<Customer> getCustomerById(Long customerId);

    /**
     * Create a new customer
     * @param customer customer data
     * @return Mono of created Customer
     */
    Mono<Customer> createCustomer(Customer customer);

    /**
     * Update existing customer
     * @param customerId customer ID to update
     * @param customer updated customer data
     * @return Mono of updated Customer
     */
    Mono<Customer> updateCustomer(Long customerId, Customer customer);

    /**
     * Delete customer
     * @param customerId customer ID to delete
     * @return Mono of Void
     */
    Mono<Void> deleteCustomer(Long customerId);
}