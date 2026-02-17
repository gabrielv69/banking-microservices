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

        // Constructor injection as required
        public MovementServiceImpl(MovementRepository movementRepository,
                        AccountService accountService) {
                this.movementRepository = movementRepository;
                this.accountService = accountService;
        }

        @Override
        public Flux<Movement> getAllMovements() {
                log.info(AccountMessages.LOG_MOVEMENT_GET_ALL);
                return movementRepository.findAllByOrderByDateDesc()
                                .doOnComplete(() -> log.info(AccountMessages.LOG_MOVEMENT_GET_ALL_SUCCESS));
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

                return accountService.getAccountById(movement.getAccountId())
                                .flatMap(account -> {
                                        BigDecimal currentBalance = account.getCurrentBalance();
                                        BigDecimal newBalance = calculateNewBalance(
                                                        movement.getMovementType(), currentBalance,
                                                        movement.getValue());

                                        // F3: Validate sufficient balance for debits
                                        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                                                return Mono.error(new InsufficientBalanceException(
                                                                AccountMessages.INSUFFICIENT_BALANCE));
                                        }

                                        movement.setBalance(newBalance);

                                        return movementRepository.save(movement)
                                                        .flatMap(savedMovement -> accountService
                                                                        .updateBalance(account.getId(), newBalance)
                                                                        .thenReturn(savedMovement))
                                                        .doOnSuccess(saved -> log.info(
                                                                        AccountMessages.LOG_MOVEMENT_REGISTERED,
                                                                        saved.getMovementType(), saved.getValue(),
                                                                        saved.getBalance()))
                                                        .doOnError(error -> log.error(
                                                                        AccountMessages.LOG_MOVEMENT_ERROR,
                                                                        error.getMessage()));
                                });
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
         * Calculates the new balance based on movement type.
         * Uses MovementType Enum for type-safe comparison.
         */
        private BigDecimal calculateNewBalance(String movementType, BigDecimal currentBalance, BigDecimal value) {
                return MovementType.DEBIT.name().equalsIgnoreCase(movementType)
                                ? currentBalance.subtract(value)
                                : currentBalance.add(value);
        }

        /**
         * Validates if the given string is a valid MovementType.
         * Uses the MovementType Enum as the single source of truth.
         */
        private boolean isValidMovementType(String movementType) {
                if (movementType == null) {
                        return false;
                }
                try {
                        MovementType.valueOf(movementType.toUpperCase());
                        return true;
                } catch (IllegalArgumentException e) {
                        return false;
                }
        }
}
