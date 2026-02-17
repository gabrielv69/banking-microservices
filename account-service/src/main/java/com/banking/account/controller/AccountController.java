package com.banking.account.controller;

import com.banking.account.constants.AccountMessages;
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
 * REST Controller for Account management.
 * Implements AccountsApi from OpenAPI specification.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class AccountController implements AccountsApi {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    public AccountController(AccountService accountService,
                             AccountMapper accountMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
    }


    @Override
    public Mono<ResponseEntity<Flux<AccountResponse>>> getAllAccounts(Long customerId,ServerWebExchange exchange) {
        log.info(AccountMessages.LOG_ACCOUNT_GET_ALL,customerId);
        Flux<AccountResponse> accounts = accountService.getAllAccounts(customerId)
                .map(accountMapper::toResponse);
        return Mono.just(ResponseEntity.ok(accounts));
    }

    @Override
    public Mono<ResponseEntity<AccountResponse>> getAccountById(Long accountId, ServerWebExchange exchange) {
        log.info(AccountMessages.LOG_ACCOUNT_GET_BY_ID, accountId);
        return accountService.getAccountById(accountId)
                .map(accountMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<AccountResponse>> createAccount(Mono<AccountRequest> accountRequest,
                                                               ServerWebExchange exchange) {
        log.info(AccountMessages.LOG_ACCOUNT_CREATE);
        return accountRequest
                .map(accountMapper::toEntity)
                .flatMap(accountService::createAccount)
                .map(accountMapper::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @Override
    public Mono<ResponseEntity<AccountResponse>> updateAccount(Long accountId, Mono<AccountRequest> accountRequest,
                                                               ServerWebExchange exchange) {
        log.info(AccountMessages.LOG_ACCOUNT_UPDATE, accountId);
        return accountRequest
                .map(accountMapper::toEntity)
                .flatMap(account -> accountService.updateAccount(accountId, account))
                .map(accountMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteAccount(Long accountId, ServerWebExchange exchange) {
        log.info(AccountMessages.LOG_ACCOUNT_DELETE, accountId);
        return accountService.deleteAccount(accountId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}