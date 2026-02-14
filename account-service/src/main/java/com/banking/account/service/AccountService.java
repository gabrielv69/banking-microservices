package com.banking.account.service;

import com.banking.account.model.Account;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Service interface for Account business logic
 */
public interface AccountService {

    /**
     * Get all accounts
     * @return Flux of all accounts
     */
    Flux<Account> getAllAccounts();

    /**
     * Get account by ID
     * @param accountId account ID
     * @return Mono of Account
     */
    Mono<Account> getAccountById(Long accountId);

    /**
     * Get account by account number
     * @param accountNumber account number
     * @return Mono of Account
     */
    Mono<Account> getAccountByNumber(String accountNumber);

    /**
     * Get all accounts by customer ID
     * @param customerId customer ID
     * @return Flux of Accounts
     */
    Flux<Account> getAccountsByCustomerId(Long customerId);

    /**
     * Create a new account
     * @param account account data
     * @return Mono of created Account
     */
    Mono<Account> createAccount(Account account);

    /**
     * Update existing account
     * @param accountId account ID to update
     * @param account updated account data
     * @return Mono of updated Account
     */
    Mono<Account> updateAccount(Long accountId, Account account);

    /**
     * Delete account
     * @param accountId account ID to delete
     * @return Mono of Void
     */
    Mono<Void> deleteAccount(Long accountId);

    /**
     * Update account balance
     * Internal use for movement transactions
     * @param accountId account ID
     * @param newBalance new balance
     * @return Mono of updated Account
     */
    Mono<Account> updateBalance(Long accountId, BigDecimal newBalance);
}






