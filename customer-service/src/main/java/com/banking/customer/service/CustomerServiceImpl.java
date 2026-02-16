package com.banking.customer.service;

import com.banking.customer.constants.CustomerMessages;
import com.banking.customer.exception.CustomerAlreadyExistsException;
import com.banking.customer.exception.CustomerNotFoundException;
import com.banking.customer.model.Customer;
import com.banking.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
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

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<Customer> getAllCustomers() {
        log.info(CustomerMessages.LOG_GETTING_ALL);
        return customerRepository.findAll()
                .doOnComplete(() -> log.info(CustomerMessages.LOG_GETTING_ALL_SUCCESS));
    }

    @Override
    public Mono<Customer> getCustomerById(Long customerId) {
        log.info(CustomerMessages.LOG_GETTING_BY_ID, customerId);
        return customerRepository.findById(customerId)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(
                        String.format(CustomerMessages.CUSTOMER_NOT_FOUND, customerId))));
    }

    @Override
    public Mono<Customer> createCustomer(Customer customer) {
        log.info(CustomerMessages.LOG_CREATING_CUSTOMER, customer.getIdentification());

        return customerRepository.existsByIdentification(customer.getIdentification())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new CustomerAlreadyExistsException(
                                String.format(CustomerMessages.CUSTOMER_ALREADY_EXISTS,
                                        customer.getIdentification())));
                    }

                    if (customer.getStatus() == null) {
                        customer.setStatus(true);
                    }

                    return customerRepository.save(customer)
                            .doOnSuccess(saved -> log.info(
                                    CustomerMessages.LOG_CREATED_SUCCESS,
                                    saved.getId()))
                            .doOnError(error -> log.error(CustomerMessages.LOG_ERROR_CREATE,
                                    error.getMessage()));
                });
    }

    @Override
    public Mono<Customer> updateCustomer(Long customerId, Customer customer) {
        log.info(CustomerMessages.LOG_UPDATING_CUSTOMER, customerId);

        return customerRepository.findById(customerId)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(
                        String.format(CustomerMessages.CUSTOMER_NOT_FOUND, customerId))))
                .flatMap(existingCustomer -> {
                    existingCustomer.setName(customer.getName());
                    existingCustomer.setGender(customer.getGender());
                    existingCustomer.setIdentification(customer.getIdentification());
                    existingCustomer.setAddress(customer.getAddress());
                    existingCustomer.setPhone(customer.getPhone());
                    existingCustomer.setPassword(customer.getPassword());
                    existingCustomer.setStatus(customer.getStatus());

                    return customerRepository.save(existingCustomer)
                            .doOnSuccess(updated -> log.info(
                                    CustomerMessages.LOG_UPDATED_SUCCESS,
                                    updated.getId()))
                            .doOnError(error -> log.error(CustomerMessages.LOG_ERROR_UPDATE,
                                    error.getMessage()));
                });
    }

    @Override
    public Mono<Void> deleteCustomer(Long customerId) {
        log.info(CustomerMessages.LOG_DELETING_CUSTOMER, customerId);

        return customerRepository.findById(customerId)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(
                        String.format(CustomerMessages.CUSTOMER_NOT_FOUND, customerId))))
                .flatMap(customer -> customerRepository.delete(customer)
                        .doOnSuccess(v -> log.info(CustomerMessages.LOG_DELETED_SUCCESS,
                                customerId))
                        .doOnError(error -> log.error(CustomerMessages.LOG_ERROR_DELETE, error.getMessage())));
    }

}