package com.banking.customer.repository;

import com.banking.customer.model.Customer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Reactive repository for Customer entity
 * Uses R2DBC for non-blocking database operations
 */
@Repository
public interface CustomerRepository extends ReactiveCrudRepository<Customer, Long> {

    /**
     * Find customer by identification number
     * 
     * @param identification unique identification number
     * @return Mono of Customer or empty if not found
     */
    Mono<Customer> findByIdentification(String identification);

    /**
     * Check if a customer exists by identification
     * 
     * @param identification unique identification number
     * @return Mono of Boolean
     */
    Mono<Boolean> existsByIdentification(String identification);
}