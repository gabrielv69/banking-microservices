package com.banking.account.controller;

import com.banking.account.constants.AccountMessages;
import com.banking.account.infrastructure.adapter.rest.generated.MovementsApi;
import com.banking.account.infrastructure.adapter.rest.generated.model.MovementRequest;
import com.banking.account.infrastructure.adapter.rest.generated.model.MovementResponse;
import com.banking.account.mapper.MovementMapper;
import com.banking.account.service.MovementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST Controller for Movement (Transaction) management.
 * Implements MovementsApi from OpenAPI specification.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class MovementController implements MovementsApi {

    private final MovementService movementService;
    private final MovementMapper movementMapper;

    public MovementController(MovementService movementService,
                              MovementMapper movementMapper) {
        this.movementService = movementService;
        this.movementMapper = movementMapper;
    }

    @Override
    public Mono<ResponseEntity<Flux<MovementResponse>>> getAllMovements(ServerWebExchange exchange) {
        log.info(AccountMessages.LOG_MOVEMENT_GET_ALL);
        Flux<MovementResponse> movements = movementService.getAllMovements()
                .map(movementMapper::toResponse);
        return Mono.just(ResponseEntity.ok(movements));
    }

    @Override
    public Mono<ResponseEntity<MovementResponse>> getMovementById(Long movementId, ServerWebExchange exchange) {
        log.info(AccountMessages.LOG_MOVEMENT_GET_BY_ID, movementId);
        return movementService.getMovementById(movementId)
                .map(movementMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<MovementResponse>> createMovement(Mono<MovementRequest> movementRequest,
                                                                 ServerWebExchange exchange) {
        log.info(AccountMessages.LOG_MOVEMENT_CREATE);
        return movementRequest
                .map(movementMapper::toEntity)
                .flatMap(movementService::registerMovement)
                .map(movementMapper::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteMovement(Long movementId, ServerWebExchange exchange) {
        log.warn(AccountMessages.LOG_MOVEMENT_DELETE_WARN, movementId);
        return movementService.deleteMovement(movementId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}