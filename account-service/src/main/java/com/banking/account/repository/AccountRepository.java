package com.banking.account.repository;


import com.banking.account.model.Account;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive repository for Account entity
 */
@Repository
public interface AccountRepository extends ReactiveCrudRepository<Account, Long> {

    /**
     * Find account by account number
     * @param accountNumber unique account number
     * @return Mono of Account or empty
     */
    Mono<Account> findByAccountNumber(String accountNumber);

    /**
     * Find all accounts by customer ID
     * @param customerId customer ID
     * @return Flux of Accounts
     */
    Flux<Account> findByCustomerId(Long customerId);

    /**
     * Check if account number already exists
     * @param accountNumber account number to check
     * @return Mono of Boolean
     */
    Mono<Boolean> existsByAccountNumber(String accountNumber);
}