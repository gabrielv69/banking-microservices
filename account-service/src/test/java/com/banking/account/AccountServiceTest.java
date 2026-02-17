package com.banking.account;

import com.banking.account.exception.AccountNotFoundException;
import com.banking.account.model.Account;
import com.banking.account.repository.AccountRepository;
import com.banking.account.service.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

/**
 * Unit tests for AccountService
 * Tests CRUD operations
 */
@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id(1L)
                .accountNumber("478758")
                .accountType("SAVINGS")
                .initialBalance(new BigDecimal("2000.00"))
                .currentBalance(new BigDecimal("2000.00"))
                .status(true)
                .customerId(1L)
                .build();
    }

    @Test
    @DisplayName("Get account by ID - should return account")
    void testGetAccountById_Success() {
        when(accountRepository.findById(anyLong())).thenReturn(Mono.just(testAccount));
        StepVerifier.create(accountService.getAccountById(1L))
                .expectNextMatches(account ->
                        account.getAccountNumber().equals("478758") &&
                                account.getCurrentBalance().compareTo(new BigDecimal("2000.00")) == 0
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Get account by ID - not found should throw exception")
    void testGetAccountById_NotFound() {
        when(accountRepository.findById(anyLong())).thenReturn(Mono.empty());
        StepVerifier.create(accountService.getAccountById(999L))
                .expectErrorMatches(error ->
                        error instanceof AccountNotFoundException &&
                                error.getMessage().contains("Account not found with ID: 999")
                )
                .verify();
    }

    @Test
    @DisplayName("Get all accounts - should return flux of accounts")
    void testGetAllAccounts_Success() {
        // Arrange
        Account account2 = Account.builder()
                .id(2L)
                .accountNumber("585545")
                .accountType("CHECKING")
                .currentBalance(new BigDecimal("1000.00"))
                .build();
        when(accountRepository.findAll()).thenReturn(Flux.just(testAccount, account2));
        StepVerifier.create(accountService.getAllAccounts(null))
                .expectNextMatches(account -> account.getAccountNumber().equals("478758"))
                .expectNextMatches(account -> account.getAccountNumber().equals("585545"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Create account - should return created account")
    void testCreateAccount_Success() {
        Account newAccount = Account.builder()
                .accountNumber("123456")
                .accountType("SAVINGS")
                .initialBalance(new BigDecimal("1500.00"))
                .customerId(1L)
                .build();

        Account savedAccount = Account.builder()
                .id(3L)
                .accountNumber("123456")
                .accountType("SAVINGS")
                .initialBalance(new BigDecimal("1500.00"))
                .currentBalance(new BigDecimal("1500.00"))
                .status(true)
                .customerId(1L)
                .build();

        when(accountRepository.existsByAccountNumber(any())).thenReturn(Mono.just(false));
        when(accountRepository.save(any(Account.class))).thenReturn(Mono.just(savedAccount));

        StepVerifier.create(accountService.createAccount(newAccount))
                .expectNextMatches(account ->
                        account.getId() == 3L &&
                                account.getCurrentBalance().compareTo(new BigDecimal("1500.00")) == 0
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Update account balance - should return updated account")
    void testUpdateBalance_Success() {
        BigDecimal newBalance = new BigDecimal("1425.00");
        Account updatedAccount = Account.builder()
                .id(1L)
                .accountNumber("478758")
                .currentBalance(newBalance)
                .build();

        when(accountRepository.findById(anyLong())).thenReturn(Mono.just(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(Mono.just(updatedAccount));
        StepVerifier.create(accountService.updateBalance(1L, newBalance))
                .expectNextMatches(account ->
                        account.getCurrentBalance().compareTo(newBalance) == 0
                )
                .verifyComplete();
    }

}
