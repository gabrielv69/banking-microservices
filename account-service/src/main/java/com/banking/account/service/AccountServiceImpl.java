package com.banking.account.service;

import com.banking.account.constants.AccountMessages;
import com.banking.account.exception.AccountAlreadyExistsException;
import com.banking.account.exception.AccountNotFoundException;
import com.banking.account.model.Account;
import com.banking.account.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Service implementation for Account business logic.
 */
@Slf4j
@Service
public class AccountServiceImpl implements AccountService {

        private final AccountRepository accountRepository;

        public AccountServiceImpl(AccountRepository accountRepository) {
                this.accountRepository = accountRepository;
        }

        @Override
        public Flux<Account> getAllAccounts() {
                log.info(AccountMessages.LOG_ACCOUNT_GET_ALL);
                return accountRepository.findAll()
                                .doOnComplete(() -> log.info(AccountMessages.LOG_ACCOUNT_GET_ALL_SUCCESS));
        }

        @Override
        public Mono<Account> getAccountById(Long accountId) {
                log.info(AccountMessages.LOG_ACCOUNT_GET_BY_ID, accountId);
                return accountRepository.findById(accountId)
                                .switchIfEmpty(Mono.error(new AccountNotFoundException(
                                                String.format(AccountMessages.ACCOUNT_NOT_FOUND, accountId))));
        }

        @Override
        public Mono<Account> getAccountByNumber(String accountNumber) {
                log.debug(AccountMessages.LOG_ACCOUNT_GET_BY_NUMBER, accountNumber);
                return accountRepository.findByAccountNumber(accountNumber)
                                .switchIfEmpty(Mono.error(new AccountNotFoundException(
                                                String.format(AccountMessages.ACCOUNT_NOT_FOUND_NUMBER,
                                                                accountNumber))));
        }

        @Override
        public Flux<Account> getAccountsByCustomerId(Long customerId) {
                log.info(AccountMessages.LOG_ACCOUNT_GET_BY_CUSTOMER, customerId);
                return accountRepository.findByCustomerId(customerId);
        }

        @Override
        public Mono<Account> createAccount(Account account) {
                log.info(AccountMessages.LOG_CREATING_ACCOUNT, account.getAccountNumber());

                if (account.getCurrentBalance() == null) {
                        account.setCurrentBalance(account.getInitialBalance());
                }
                if (account.getStatus() == null) {
                        account.setStatus(true);
                }

                return accountRepository.existsByAccountNumber(account.getAccountNumber())
                                .flatMap(exists -> {
                                        if (Boolean.TRUE.equals(exists)) {
                                                return Mono.error(new AccountAlreadyExistsException(
                                                                String.format(AccountMessages.ACCOUNT_ALREADY_EXISTS,
                                                                                account.getAccountNumber())));
                                        }
                                        return accountRepository.save(account)
                                                        .doOnSuccess(saved -> log.info(
                                                                        AccountMessages.LOG_ACCOUNT_CREATED,
                                                                        saved.getId()));
                                });
        }

        @Override
        public Mono<Account> updateAccount(Long accountId, Account account) {
                log.info(AccountMessages.LOG_UPDATING_ACCOUNT, accountId);

                return accountRepository.findById(accountId)
                                .switchIfEmpty(Mono.error(new AccountNotFoundException(
                                                String.format(AccountMessages.ACCOUNT_NOT_FOUND, accountId))))
                                .flatMap(existingAccount -> {
                                        existingAccount.setAccountNumber(account.getAccountNumber());
                                        existingAccount.setAccountType(account.getAccountType());
                                        existingAccount.setInitialBalance(account.getInitialBalance());
                                        existingAccount.setCurrentBalance(account.getCurrentBalance());
                                        existingAccount.setStatus(account.getStatus());
                                        existingAccount.setCustomerId(account.getCustomerId());

                                        return accountRepository.save(existingAccount)
                                                        .doOnSuccess(updated -> log.info(
                                                                        AccountMessages.LOG_ACCOUNT_UPDATED,
                                                                        updated.getId()))
                                                        .doOnError(error -> log.error(
                                                                        AccountMessages.LOG_ACCOUNT_ERROR_UPDATE,
                                                                        accountId, error.getMessage()));
                                });
        }

        @Override
        public Mono<Void> deleteAccount(Long accountId) {
                log.info(AccountMessages.LOG_DELETING_ACCOUNT, accountId);

                return accountRepository.findById(accountId)
                                .switchIfEmpty(Mono.error(new AccountNotFoundException(
                                                String.format(AccountMessages.ACCOUNT_NOT_FOUND, accountId))))
                                .flatMap(account -> accountRepository.delete(account)
                                                .doOnSuccess(v -> log.info(AccountMessages.LOG_ACCOUNT_DELETED,
                                                                accountId))
                                                .doOnError(error -> log.error(AccountMessages.LOG_ACCOUNT_ERROR_DELETE,
                                                                accountId, error.getMessage())));
        }

        @Override
        public Mono<Account> updateBalance(Long accountId, BigDecimal newBalance) {
                log.debug(AccountMessages.LOG_BALANCE_UPDATED, accountId, newBalance);

                return accountRepository.findById(accountId)
                                .switchIfEmpty(Mono.error(new AccountNotFoundException(
                                                String.format(AccountMessages.ACCOUNT_NOT_FOUND, accountId))))
                                .flatMap(account -> {
                                        account.setCurrentBalance(newBalance);
                                        return accountRepository.save(account);
                                });
        }
}
