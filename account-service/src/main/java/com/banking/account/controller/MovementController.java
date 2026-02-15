package com.banking.account.controller;

import com.banking.account.infrastructure.adapter.rest.generated.MovementsApi;
import com.banking.account.infrastructure.adapter.rest.generated.model.MovementRequest;
import com.banking.account.infrastructure.adapter.rest.generated.model.MovementResponse;
import com.banking.account.mapper.MovementMapper;
import com.banking.account.service.MovementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST Controller for Movement (Transaction) management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class MovementController implements MovementsApi {

    private final MovementService movementService;
    private final MovementMapper movementMapper;

    /**
     * Constructor injection for dependencies
     *
     * @param movementService service layer containing business logic
     * @param movementMapper mapper for entity-DTO conversions
     */
    public MovementController(MovementService movementService,
                              MovementMapper movementMapper) {
        this.movementService = movementService;
        this.movementMapper = movementMapper;
    }

    /**
     * GET /api/v1/movements
     * Retrieve all movements (transactions)
     *
     * @param exchange WebFlux server exchange
     * @return ResponseEntity with Flux of movements
     */
    @Override
    public Mono<ResponseEntity<Flux<MovementResponse>>> getAllMovements(ServerWebExchange exchange) {
        log.info("GET /api/v1/movements - Fetching all movements");

        Flux<MovementResponse> movements = movementService.getAllMovements()
                .map(movementMapper::toResponse)
                .doOnComplete(() -> log.info("Successfully retrieved all movements"));

        return Mono.just(ResponseEntity.ok(movements));
    }

    /**
     * GET /api/v1/movements/{movementId}
     * Retrieve specific movement by ID
     *
     * @param movementId movement identifier
     * @param exchange WebFlux server exchange
     * @return ResponseEntity with movement data
     */
    @Override
    public Mono<ResponseEntity<MovementResponse>> getMovementById(Long movementId,
                                                                  ServerWebExchange exchange) {
        log.info("GET /api/v1/movements/{} - Fetching movement", movementId);

        return movementService.getMovementById(movementId)
                .map(movementMapper::toResponse)
                .map(ResponseEntity::ok)
                .doOnSuccess(response ->
                        log.info("Movement found with ID: {}", movementId))
                .doOnError(error ->
                        log.error("Error fetching movement {}: {}", movementId, error.getMessage()));
    }

    /**
     * POST /api/v1/movements
     * Register a new movement (transaction)
     *
     * @param movementRequest transaction data (accountId, type, value)
     * @param exchange WebFlux server exchange
     * @return ResponseEntity with registered movement (201 CREATED)
     */
    @Override
    public Mono<ResponseEntity<MovementResponse>> createMovement(Mono<MovementRequest> movementRequest,
                                                                 ServerWebExchange exchange) {
        log.info("POST /api/v1/movements - Registering new movement");

        return movementRequest
                .doOnNext(request ->
                        log.info("Movement request - AccountId: {}, Type: {}, Value: {}",
                                request.getAccountId(),
                                request.getMovementType(),
                                request.getValue()))
                .map(movementMapper::toEntity)
                .flatMap(movementService::registerMovement)
                .map(movementMapper::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .doOnSuccess(response -> {
                    MovementResponse body = response.getBody();
                    log.info("Movement registered successfully - ID: {}, Type: {}, Value: {}, New Balance: {}",
                            body.getMovementId(),
                            body.getMovementType(),
                            body.getValue(),
                            body.getBalance());
                })
                .doOnError(error ->
                        log.error("Error registering movement: {}", error.getMessage()));
    }

    /**
     * DELETE /api/v1/movements/{movementId}
     * Delete a movement
     *
     * @param movementId movement to delete
     * @param exchange WebFlux server exchange
     * @return ResponseEntity with no content (204 NO CONTENT)
     */
    @Override
    public Mono<ResponseEntity<Void>> deleteMovement(Long movementId,
                                                     ServerWebExchange exchange) {
        log.warn("DELETE /api/v1/movements/{} - Deleting movement (consider reversal instead)",
                movementId);

        return movementService.deleteMovement(movementId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .doOnSuccess(response ->
                        log.info("Movement deleted with ID: {}", movementId))
                .doOnError(error ->
                        log.error("Error deleting movement {}: {}", movementId, error.getMessage()));
    }
}