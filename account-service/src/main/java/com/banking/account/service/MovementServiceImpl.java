package com.banking.account.service;

import com.banking.account.constants.AccountMessages;
import com.banking.account.exception.InsufficientBalanceException;
import com.banking.account.exception.InvalidMovementException;
import com.banking.account.exception.MovementNotFoundException;
import com.banking.account.model.Movement;
import com.banking.account.model.MovementType;
import com.banking.account.repository.MovementRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Service implementation for Movement business logic.
 * Handles movement registration with balance calculation and validation.
 */
@Slf4j
@Service
public class MovementServiceImpl implements MovementService {

    private final MovementRepository movementRepository;
    private final AccountService accountService;

    public MovementServiceImpl(MovementRepository movementRepository,
                               AccountService accountService) {
        this.movementRepository = movementRepository;
        this.accountService = accountService;
    }

    /**
     * Get all movements with optional filtering by customer.
     * When customerId is provided:
     * - Fetch all accounts for that customer
     * - Fetch movements per account
     * - Merge and sort DESC by date (consistent with no-filter behavior)
     *
     * @param customerId Optional customer filter (null = all movements)
     * @return Flux of Movements ordered by date DESC
     */
    @Override
    public Flux<Movement> getAllMovements(Long customerId) {
        if (customerId != null) {
            log.info(AccountMessages.LOG_MOVEMENT_GET_BY_CUSTOMER, customerId);
            return accountService.getAllAccounts(customerId)
                    .flatMap(account -> movementRepository.findByAccountId(account.getId()))
                    .sort((m1, m2) -> m2.getDate().compareTo(m1.getDate()))
                    .doOnComplete(() -> log.info(AccountMessages.LOG_MOVEMENT_GET_BY_CUSTOMER_SUCCESS, customerId));
        } else {
            log.info(AccountMessages.LOG_MOVEMENT_GET_ALL);
            return movementRepository.findAllByOrderByDateDesc()
                    .doOnComplete(() -> log.info(AccountMessages.LOG_MOVEMENT_GET_ALL_SUCCESS));
        }
    }

    @Override
    public Mono<Movement> getMovementById(Long movementId) {
        log.info(AccountMessages.LOG_MOVEMENT_GET_BY_ID, movementId);
        return movementRepository.findById(movementId)
                .switchIfEmpty(Mono.error(new MovementNotFoundException(
                        String.format(AccountMessages.MOVEMENT_NOT_FOUND, movementId))));
    }

    @Override
    public Flux<Movement> getMovementsByAccountId(Long accountId) {
        log.debug(AccountMessages.LOG_MOVEMENT_GET_BY_ACCOUNT, accountId);
        return movementRepository.findByAccountId(accountId);
    }

    @Override
    public Mono<Movement> registerMovement(Movement movement) {
        log.info(AccountMessages.LOG_REGISTERING_MOVEMENT,
                movement.getMovementType(), movement.getValue(), movement.getAccountId());

        // F2: Validate value > 0
        if (movement.getValue() == null || movement.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(
                    new InvalidMovementException(AccountMessages.MOVEMENT_VALUE_MUST_BE_POSITIVE));
        }

        // Validate movement type using MovementType Enum
        if (!isValidMovementType(movement.getMovementType())) {
            return Mono.error(new InvalidMovementException(
                    String.format(AccountMessages.INVALID_MOVEMENT_TYPE,
                            movement.getMovementType())));
        }

        if (movement.getDate() == null) {
            movement.setDate(LocalDateTime.now());
        }

        return resolveAccount(movement)
                .flatMap(this::processMovement);
    }

    @Override
    public Mono<Void> deleteMovement(Long movementId) {
        log.info(AccountMessages.LOG_MOVEMENT_DELETE, movementId);

        return movementRepository.findById(movementId)
                .switchIfEmpty(Mono.error(new MovementNotFoundException(
                        String.format(AccountMessages.MOVEMENT_NOT_FOUND, movementId))))
                .flatMap(movement -> movementRepository.delete(movement)
                        .doOnSuccess(v -> log.info(AccountMessages.LOG_MOVEMENT_DELETED_SUCCESS,
                                movementId)));
    }

    @Override
    public Flux<Movement> getMovementsByAccountAndDateRange(Long accountId, LocalDate startDate,
                                                            LocalDate endDate) {
        log.debug(AccountMessages.LOG_MOVEMENT_GET_BY_DATE_RANGE, accountId, startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        return movementRepository.findByAccountIdAndDateBetween(accountId, startDateTime, endDateTime);
    }

    /**
     * Resolves the account for a movement request.
     * <p>
     * Priority:
     * 1. accountNumber → look up account → set accountId on movement
     * 2. accountId     → use directly (already set)
     * 3. neither       → InvalidMovementException
     * <p>
     * Single Responsibility: Only resolves which account to use.
     *
     * @param movement Movement with accountNumber or accountId
     * @return Mono of Movement with accountId guaranteed to be set
     */
    private Mono<Movement> resolveAccount(Movement movement) {
        if (movement.getAccountNumber() != null && !movement.getAccountNumber().isBlank()) {
            log.debug("Resolving account by accountNumber: {}", movement.getAccountNumber());
            return accountService.getAccountByNumber(movement.getAccountNumber())
                    .map(account -> {
                        movement.setAccountId(account.getId());
                        log.debug("Resolved accountNumber {} → accountId {}",
                                movement.getAccountNumber(), account.getId());
                        return movement;
                    });
        }

        if (movement.getAccountId() != null) {
            log.debug("Using provided accountId: {}", movement.getAccountId());
            return Mono.just(movement);
        }

        return Mono.error(new InvalidMovementException(
                AccountMessages.MOVEMENT_ACCOUNT_IDENTIFIER_REQUIRED));
    }

    /**
     * Processes a movement once the account has been resolved.
     * Validates F3, calculates new balance, persists movement and updates account.
     * <p>
     * Single Responsibility: Only processes movement. Does NOT resolve account.
     *
     * @param movement Movement with accountId set
     * @return Mono of persisted Movement
     */
    private Mono<Movement> processMovement(Movement movement) {
        return accountService.getAccountById(movement.getAccountId())
                .flatMap(account -> {
                    BigDecimal currentBalance = account.getCurrentBalance();
                    BigDecimal newBalance = calculateNewBalance(
                            movement.getMovementType(), currentBalance, movement.getValue());

                    // F3: Validate sufficient balance for debits
                    if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                        log.error("Insufficient balance. Account: {}, Current: {}, Requested: {}",
                                account.getAccountNumber(), currentBalance, movement.getValue());
                        return Mono.error(new InsufficientBalanceException(
                                AccountMessages.INSUFFICIENT_BALANCE));
                    }

                    movement.setBalance(newBalance);

                    return movementRepository.save(movement)
                            .flatMap(saved ->
                                    accountService.updateBalance(account.getId(), newBalance)
                                            .thenReturn(saved))
                            .doOnSuccess(saved -> log.info(
                                    AccountMessages.LOG_MOVEMENT_REGISTERED,
                                    saved.getMovementType(), saved.getValue(), saved.getBalance()))
                            .doOnError(error -> log.error(
                                    AccountMessages.LOG_MOVEMENT_ERROR, error.getMessage()));
                });
    }

    /**
     * Calculates the new balance based on movement type.
     * CREDIT adds, DEBIT subtracts.
     */
    private BigDecimal calculateNewBalance(String movementType, BigDecimal currentBalance, BigDecimal value) {
        return MovementType.DEBIT.name().equalsIgnoreCase(movementType)
                ? currentBalance.subtract(value)
                : currentBalance.add(value);
    }

    /**
     * Validates movement type using MovementType enum as single source of truth.
     */
    private boolean isValidMovementType(String movementType) {
        if (movementType == null) return false;
        try {
            MovementType.valueOf(movementType.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
