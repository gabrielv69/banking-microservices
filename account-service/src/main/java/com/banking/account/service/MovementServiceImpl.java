package com.banking.account.service;

import com.banking.account.exception.InsufficientBalanceException;
import com.banking.account.exception.InvalidMovementException;
import com.banking.account.exception.MovementNotFoundException;
import com.banking.account.model.Movement;
import com.banking.account.repository.MovementRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Service implementation for Movement business logic
 */
@Slf4j
@Service
public class MovementServiceImpl implements MovementService {

    private final MovementRepository movementRepository;
    private final AccountService accountService;

    // Constructor injection as required
    public MovementServiceImpl(MovementRepository movementRepository,
                               AccountService accountService) {
        this.movementRepository = movementRepository;
        this.accountService = accountService;
    }

    @Override
    public Flux<Movement> getAllMovements() {
        log.info("Fetching all movements");
        return movementRepository.findAllByOrderByDateDesc()
                .doOnNext(movement -> log.debug("Found movement ID: {}", movement.getId()))
                .doOnComplete(() -> log.info("Successfully fetched all movements"));
    }

    @Override
    public Mono<Movement> getMovementById(Long movementId) {
        log.info("Fetching movement with ID: {}", movementId);
        return movementRepository.findById(movementId)
                .doOnNext(movement -> log.debug("Found movement: {}", movement.getId()))
                .switchIfEmpty(Mono.error(new MovementNotFoundException(
                        "Movement not found with ID: " + movementId)))
                .doOnError(error -> log.error("Error fetching movement with ID {}: {}",
                        movementId, error.getMessage()));
    }

    @Override
    public Flux<Movement> getMovementsByAccountId(Long accountId) {
        log.info("Fetching movements for account ID: {}", accountId);
        return movementRepository.findByAccountId(accountId)
                .doOnNext(movement -> log.debug("Found movement: {}", movement.getId()))
                .doOnComplete(() -> log.info("Successfully fetched movements for account {}", accountId));
    }

    @Override
    public Mono<Movement> registerMovement(Movement movement) {
        log.info("Registering new movement for account ID: {}", movement.getAccountId());

        // F2 REQUIREMENT: Validate that value is greater than zero
        if (movement.getValue() == null || movement.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Invalid movement value: {}", movement.getValue());
            return Mono.error(new InvalidMovementException(
                    "El valor de un movimiento debe ser mayor que cero"));
        }

        // Validate movement type
        if (!isValidMovementType(movement.getMovementType())) {
            log.error("Invalid movement type: {}", movement.getMovementType());
            return Mono.error(new InvalidMovementException(
                    "Movement type must be CREDIT or DEBIT"));
        }

        // Set movement date
        if (movement.getDate() == null) {
            movement.setDate(LocalDateTime.now());
        }

        // Get current account and process the movement
        return accountService.getAccountById(movement.getAccountId())
                .flatMap(account -> {
                    BigDecimal currentBalance = account.getCurrentBalance();
                    BigDecimal newBalance;

                    // F2 REQUIREMENT: Process movement based on type
                    if ("DEBIT".equalsIgnoreCase(movement.getMovementType())) {
                        // DEBIT: Subtract from available balance
                        newBalance = currentBalance.subtract(movement.getValue());

                        // F3 REQUIREMENT: Validate sufficient balance
                        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                            log.error("Insufficient balance for account {}. Current: {}, Requested: {}",
                                    account.getAccountNumber(), currentBalance, movement.getValue());
                            return Mono.error(new InsufficientBalanceException(
                                    "Saldo no disponible"));
                        }

                        log.debug("DEBIT processed: {} - {} = {}", currentBalance, movement.getValue(), newBalance);

                    } else if ("CREDIT".equalsIgnoreCase(movement.getMovementType())) {
                        // CREDIT: Add to available balance
                        newBalance = currentBalance.add(movement.getValue());
                        log.debug("CREDIT processed: {} + {} = {}", currentBalance, movement.getValue(), newBalance);

                    } else {
                        return Mono.error(new InvalidMovementException(
                                "Invalid movement type: " + movement.getMovementType()));
                    }

                    // Set the balance after this movement
                    movement.setBalance(newBalance);

                    // F2 REQUIREMENT: Register each transaction
                    return movementRepository.save(movement)
                            .flatMap(savedMovement ->
                                    // Update account balance
                                    accountService.updateBalance(account.getId(), newBalance)
                                            .thenReturn(savedMovement)
                            )
                            .doOnSuccess(saved -> log.info(
                                    "Movement registered successfully. ID: {}, Type: {}, Value: {}, New Balance: {}",
                                    saved.getId(), saved.getMovementType(), saved.getValue(), saved.getBalance()))
                            .doOnError(error -> log.error("Error registering movement: {}",
                                    error.getMessage()));
                });
    }

    @Override
    public Mono<Void> deleteMovement(Long movementId) {
        log.info("Deleting movement with ID: {}", movementId);

        return movementRepository.findById(movementId)
                .switchIfEmpty(Mono.error(new MovementNotFoundException(
                        "Movement not found with ID: " + movementId)))
                .flatMap(movement -> movementRepository.delete(movement)
                        .doOnSuccess(v -> log.info("Movement deleted successfully with ID: {}",
                                movementId))
                        .doOnError(error -> log.error("Error deleting movement with ID {}: {}",
                                movementId, error.getMessage())));
    }

    @Override
    public Flux<Movement> getMovementsByAccountAndDateRange(Long accountId, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching movements for account {} between {} and {}", accountId, startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        return movementRepository.findByAccountIdAndDateBetween(accountId, startDateTime, endDateTime)
                .doOnNext(movement -> log.debug("Found movement: {}", movement.getId()))
                .doOnComplete(() -> log.info("Successfully fetched movements for date range"));
    }


    /**
     * Validate if movement type is valid
     * @param movementType movement type
     * @return true if valid, false otherwise
     */
    private boolean isValidMovementType(String movementType) {
        return movementType != null &&
                (movementType.equalsIgnoreCase("CREDIT") || movementType.equalsIgnoreCase("DEBIT"));
    }
}
