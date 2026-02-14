package com.banking.account.service;

import com.banking.account.exception.AccountNotFoundException;
import com.banking.account.model.Account;
import com.banking.account.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Service implementation for Account business logic
 */
@Slf4j
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<Account> getAllAccounts() {
        log.info("Fetching all accounts");
        return accountRepository.findAll()
                .doOnNext(account -> log.debug("Found account: {}", account.getAccountNumber()))
                .doOnComplete(() -> log.info("Successfully fetched all accounts"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Account> getAccountById(Long accountId) {
        log.info("Fetching account with ID: {}", accountId);
        return accountRepository.findById(accountId)
                .doOnNext(account -> log.debug("Found account: {}", account.getAccountNumber()))
                .switchIfEmpty(Mono.error(new AccountNotFoundException(
                        "Account not found with ID: " + accountId)))
                .doOnError(error -> log.error("Error fetching account with ID {}: {}",
                        accountId, error.getMessage()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Account> getAccountByNumber(String accountNumber) {
        log.info("Fetching account with number: {}", accountNumber);
        return accountRepository.findByAccountNumber(accountNumber)
                .doOnNext(account -> log.debug("Found account: {}", account.getAccountNumber()))
                .switchIfEmpty(Mono.error(new AccountNotFoundException(
                        "Account not found with number: " + accountNumber)))
                .doOnError(error -> log.error("Error fetching account {}: {}",
                        accountNumber, error.getMessage()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<Account> getAccountsByCustomerId(Long customerId) {
        log.info("Fetching accounts for customer ID: {}", customerId);
        return accountRepository.findByCustomerId(customerId)
                .doOnNext(account -> log.debug("Found account: {}", account.getAccountNumber()))
                .doOnComplete(() -> log.info("Successfully fetched accounts for customer {}", customerId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Account> createAccount(Account account) {
        log.info("Creating new account: {}", account.getAccountNumber());

        // Set initial balance as current balance if not set
        if (account.getCurrentBalance() == null) {
            account.setCurrentBalance(account.getInitialBalance());
        }

        // Set default status if not provided
        if (account.getStatus() == null) {
            account.setStatus(true);
        }

        return accountRepository.existsByAccountNumber(account.getAccountNumber())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        log.warn("Account already exists: {}", account.getAccountNumber());
                        return Mono.error(new IllegalArgumentException(
                                "Account already exists with number: " + account.getAccountNumber()));
                    }

                    return accountRepository.save(account)
                            .doOnSuccess(saved -> log.info("Account created successfully with ID: {}",
                                    saved.getId()))
                            .doOnError(error -> log.error("Error creating account: {}",
                                    error.getMessage()));
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Account> updateAccount(Long accountId, Account account) {
        log.info("Updating account with ID: {}", accountId);

        return accountRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new AccountNotFoundException(
                        "Account not found with ID: " + accountId)))
                .flatMap(existingAccount -> {
                    // Update fields
                    existingAccount.setAccountNumber(account.getAccountNumber());
                    existingAccount.setAccountType(account.getAccountType());
                    existingAccount.setInitialBalance(account.getInitialBalance());
                    existingAccount.setCurrentBalance(account.getCurrentBalance());
                    existingAccount.setStatus(account.getStatus());
                    existingAccount.setCustomerId(account.getCustomerId());

                    return accountRepository.save(existingAccount)
                            .doOnSuccess(updated -> log.info("Account updated successfully with ID: {}",
                                    updated.getId()))
                            .doOnError(error -> log.error("Error updating account with ID {}: {}",
                                    accountId, error.getMessage()));
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> deleteAccount(Long accountId) {
        log.info("Deleting account with ID: {}", accountId);

        return accountRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new AccountNotFoundException(
                        "Account not found with ID: " + accountId)))
                .flatMap(account -> accountRepository.delete(account)
                        .doOnSuccess(v -> log.info("Account deleted successfully with ID: {}",
                                accountId))
                        .doOnError(error -> log.error("Error deleting account with ID {}: {}",
                                accountId, error.getMessage())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Account> updateBalance(Long accountId, BigDecimal newBalance) {
        log.debug("Updating balance for account ID {}: {}", accountId, newBalance);

        return accountRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new AccountNotFoundException(
                        "Account not found with ID: " + accountId)))
                .flatMap(account -> {
                    account.setCurrentBalance(newBalance);
                    return accountRepository.save(account)
                            .doOnSuccess(updated -> log.debug("Balance updated for account {}: {}",
                                    accountId, newBalance));
                });
    }
}
