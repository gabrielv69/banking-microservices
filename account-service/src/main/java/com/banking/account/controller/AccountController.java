package com.banking.account.controller;

import com.banking.account.infrastructure.adapter.rest.generated.AccountsApi;
import com.banking.account.infrastructure.adapter.rest.generated.model.AccountRequest;
import com.banking.account.infrastructure.adapter.rest.generated.model.AccountResponse;
import com.banking.account.mapper.AccountMapper;
import com.banking.account.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST Controller for Account management
 *
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class AccountController implements AccountsApi {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    /**
     * Constructor injection
     *
     * @param accountService service layer for business logic
     * @param accountMapper mapper for entity-DTO conversion
     */
    public AccountController(AccountService accountService,
                             AccountMapper accountMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
    }

    /**
     * GET /api/v1/accounts
     * Retrieve all accounts
     *
     *
     * @param exchange WebFlux server exchange
     * @return ResponseEntity with Flux of accounts
     */
    @Override
    public Mono<ResponseEntity<Flux<AccountResponse>>> getAllAccounts(ServerWebExchange exchange) {
        log.info("GET /api/v1/accounts - Fetching all accounts");

        Flux<AccountResponse> accounts = accountService.getAllAccounts()
                .map(accountMapper::toResponse)
                .doOnComplete(() -> log.info("Successfully retrieved all accounts"));

        return Mono.just(ResponseEntity.ok(accounts));
    }

    /**
     * GET /api/v1/accounts/{accountId}
     * Retrieve specific account by ID
     *
     * @param accountId account identifier
     * @param exchange WebFlux server exchange
     * @return ResponseEntity with account data
     */
    @Override
    public Mono<ResponseEntity<AccountResponse>> getAccountById(Long accountId,
                                                                ServerWebExchange exchange) {
        log.info("GET /api/v1/accounts/{} - Fetching account", accountId);

        return accountService.getAccountById(accountId)
                .map(accountMapper::toResponse)
                .map(ResponseEntity::ok)
                .doOnSuccess(response ->
                        log.info("Account found with ID: {}", accountId))
                .doOnError(error ->
                        log.error("Error fetching account {}: {}", accountId, error.getMessage()));
    }

    /**
     * POST /api/v1/accounts
     * Create a new account
     *
     * @param accountRequest account creation data
     * @param exchange WebFlux server exchange
     * @return ResponseEntity with created account (201 CREATED)
     */
    @Override
    public Mono<ResponseEntity<AccountResponse>> createAccount(Mono<AccountRequest> accountRequest,
                                                               ServerWebExchange exchange) {
        log.info("POST /api/v1/accounts - Creating new account");

        return accountRequest
                .map(accountMapper::toEntity)  // DTO -> Entity
                .flatMap(accountService::createAccount)  // Business logic
                .map(accountMapper::toResponse)  // Entity -> DTO
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))  // 201
                .doOnSuccess(response ->
                        log.info("Account created successfully with ID: {}",
                                response.getBody().getAccountId()))
                .doOnError(error ->
                        log.error("Error creating account: {}", error.getMessage()));
    }

    /**
     * PUT /api/v1/accounts/{accountId}
     * Update existing account
     *
     * @param accountId account to update
     * @param accountRequest updated account data
     * @param exchange WebFlux server exchange
     * @return ResponseEntity with updated account (200 OK)
     */
    @Override
    public Mono<ResponseEntity<AccountResponse>> updateAccount(Long accountId,
                                                               Mono<AccountRequest> accountRequest,
                                                               ServerWebExchange exchange) {
        log.info("PUT /api/v1/accounts/{} - Updating account", accountId);

        return accountRequest
                .map(accountMapper::toEntity)
                .flatMap(account -> accountService.updateAccount(accountId, account))
                .map(accountMapper::toResponse)
                .map(ResponseEntity::ok)
                .doOnSuccess(response ->
                        log.info("Account updated successfully with ID: {}", accountId))
                .doOnError(error ->
                        log.error("Error updating account {}: {}", accountId, error.getMessage()));
    }

    /**
     * DELETE /api/v1/accounts/{accountId}
     * Delete an account
     *
     * @param accountId account to delete
     * @param exchange WebFlux server exchange
     * @return ResponseEntity with no content (204 NO CONTENT)
     */
    @Override
    public Mono<ResponseEntity<Void>> deleteAccount(Long accountId,
                                                    ServerWebExchange exchange) {
        log.info("DELETE /api/v1/accounts/{} - Deleting account", accountId);

        return accountService.deleteAccount(accountId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))  // 204
                .doOnSuccess(response ->
                        log.info("Account deleted successfully with ID: {}", accountId))
                .doOnError(error ->
                        log.error("Error deleting account {}: {}", accountId, error.getMessage()));
    }
}