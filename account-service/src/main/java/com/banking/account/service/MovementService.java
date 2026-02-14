package com.banking.account.service;


import com.banking.account.model.Movement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * Service interface for Movement business logic
 * Implements requirements F2 and F3
 */
public interface MovementService {

    /**
     * Get all movements
     * @return Flux of all movements
     */
    Flux<Movement> getAllMovements();

    /**
     * Get movement by ID
     * @param movementId movement ID
     * @return Mono of Movement
     */
    Mono<Movement> getMovementById(Long movementId);

    /**
     * Get all movements by account ID
     * @param accountId account ID
     * @return Flux of Movements
     */
    Flux<Movement> getMovementsByAccountId(Long accountId);

    /**
     * Register a new movement (transaction)
     * Implements F2: Business rules for movements
     * Implements F3: Validation of available balance
     *
     * @param movement movement data (accountId, type, value)
     * @return Mono of created Movement with updated balance
     */
    Mono<Movement> registerMovement(Movement movement);

    /**
     * Delete movement
     * @param movementId movement ID to delete
     * @return Mono of Void
     */
    Mono<Void> deleteMovement(Long movementId);

    /**
     * Get movements by account and date range
     * Used for account statement reports (F4)
     *
     * @param accountId account ID
     * @param startDate start date
     * @param endDate end date
     * @return Flux of Movements
     */
    Flux<Movement> getMovementsByAccountAndDateRange(Long accountId, LocalDate startDate, LocalDate endDate);
}






