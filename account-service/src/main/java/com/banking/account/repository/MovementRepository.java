package com.banking.account.repository;

import com.banking.account.model.Movement;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

/**
 * Reactive repository for Movement entity
 */
@Repository
public interface MovementRepository extends ReactiveCrudRepository<Movement, Long> {

    /**
     * Find all movements by account ID
     * @param accountId account ID
     * @return Flux of Movements
     */
    Flux<Movement> findByAccountId(Long accountId);

    /**
     * Find movements by account ID and date range
     * @param accountId account ID
     * @param startDate start date
     * @param endDate end date
     * @return Flux of Movements
     */
    Flux<Movement> findByAccountIdAndDateBetween(Long accountId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find all movements ordered by date descending
     * @return Flux of Movements
     */
    Flux<Movement> findAllByOrderByDateDesc();
}