package com.banking.account;

import com.banking.account.exception.InsufficientBalanceException;
import com.banking.account.exception.InvalidMovementException;
import com.banking.account.model.Account;
import com.banking.account.model.Movement;
import com.banking.account.repository.MovementRepository;
import com.banking.account.service.AccountService;
import com.banking.account.service.MovementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Unit tests for MovementService
 * Tests critical business logic
 */
@ExtendWith(MockitoExtension.class)
class MovementServiceTest {

    @Mock
    private MovementRepository movementRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private MovementServiceImpl movementService;

    private Account testAccount;
    private Movement testMovement;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id(1L)
                .accountNumber("478758")
                .accountType("SAVINGS")
                .currentBalance(new BigDecimal("2000.00"))
                .build();

        testMovement = Movement.builder()
                .id(1L)
                .accountId(1L)
                .movementType("DEBIT")
                .value(new BigDecimal("575.00"))
                .build();
    }

    /**
     * F2: Test successful DEBIT movement
     * Validates that debit subtracts from balance correctly
     */
    @Test
    @DisplayName("Register DEBIT movement - should subtract from balance")
    void testRegisterDebitMovement_Success() {
        Movement savedMovement = Movement.builder()
                .id(1L)
                .accountId(1L)
                .movementType("DEBIT")
                .value(new BigDecimal("575.00"))
                .balance(new BigDecimal("1425.00"))  // 2000 - 575
                .build();

        when(accountService.getAccountById(anyLong())).thenReturn(Mono.just(testAccount));
        when(movementRepository.save(any(Movement.class))).thenReturn(Mono.just(savedMovement));
        when(accountService.updateBalance(anyLong(), any(BigDecimal.class)))
                .thenReturn(Mono.just(testAccount));

        StepVerifier.create(movementService.registerMovement(testMovement))
                .expectNextMatches(movement ->
                        movement.getBalance().compareTo(new BigDecimal("1425.00")) == 0
                )
                .verifyComplete();
    }

    /**
     * F2: Test successful CREDIT movement
     * Validates that credit adds to balance correctly
     */
    @Test
    @DisplayName("F2: Register CREDIT movement - should add to balance")
    void testRegisterCreditMovement_Success() {
        Movement creditMovement = Movement.builder()
                .accountId(1L)
                .movementType("CREDIT")
                .value(new BigDecimal("1000.00"))
                .build();

        Movement savedMovement = Movement.builder()
                .id(2L)
                .accountId(1L)
                .movementType("CREDIT")
                .value(new BigDecimal("1000.00"))
                .balance(new BigDecimal("3000.00"))  // 2000 + 1000
                .build();

        when(accountService.getAccountById(anyLong())).thenReturn(Mono.just(testAccount));
        when(movementRepository.save(any(Movement.class))).thenReturn(Mono.just(savedMovement));
        when(accountService.updateBalance(anyLong(), any(BigDecimal.class)))
                .thenReturn(Mono.just(testAccount));

        StepVerifier.create(movementService.registerMovement(creditMovement))
                .expectNextMatches(movement ->
                        movement.getBalance().compareTo(new BigDecimal("3000.00")) == 0
                )
                .verifyComplete();
    }

    /**
     * F3: Test insufficient balance validation
     * Validates "Saldo no disponible" error when balance is insufficient
     */
    @Test
    @DisplayName("F3: Register DEBIT with insufficient balance - should fail with 'Saldo no disponible'")
    void testRegisterDebitMovement_InsufficientBalance() {
        // Arrange: Try to debit 3000 when balance is only 2000
        Movement largeDebit = Movement.builder()
                .accountId(1L)
                .movementType("DEBIT")
                .value(new BigDecimal("3000.00"))  // More than balance
                .build();

        when(accountService.getAccountById(anyLong())).thenReturn(Mono.just(testAccount));

        // Act & Assert: Should throw InsufficientBalanceException
        StepVerifier.create(movementService.registerMovement(largeDebit))
                .expectErrorMatches(error ->
                        error instanceof InsufficientBalanceException &&
                                error.getMessage().equals("Saldo no disponible")
                )
                .verify();
    }

    /**
     * F2: Test validation - value must be greater than zero
     */
    @Test
    @DisplayName("F2: Register movement with zero value - should fail")
    void testRegisterMovement_ZeroValue() {
        Movement invalidMovement = Movement.builder()
                .accountId(1L)
                .movementType("DEBIT")
                .value(BigDecimal.ZERO)  // Invalid: zero value
                .build();

        StepVerifier.create(movementService.registerMovement(invalidMovement))
                .expectErrorMatches(error ->
                        error instanceof InvalidMovementException &&
                                error.getMessage().contains("debe ser mayor que cero")
                )
                .verify();
    }

    /**
     * F2: Test validation - value must be positive
     */
    @Test
    @DisplayName("F2: Register movement with negative value - should fail")
    void testRegisterMovement_NegativeValue() {
        // Arrange
        Movement invalidMovement = Movement.builder()
                .accountId(1L)
                .movementType("DEBIT")
                .value(new BigDecimal("-100.00"))  // Invalid: negative value
                .build();

        StepVerifier.create(movementService.registerMovement(invalidMovement))
                .expectErrorMatches(error ->
                        error instanceof InvalidMovementException
                )
                .verify();
    }
}
