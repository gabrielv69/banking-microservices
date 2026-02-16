package com.banking.account.mapper;

import com.banking.account.infrastructure.adapter.rest.generated.model.AccountRequest;
import com.banking.account.infrastructure.adapter.rest.generated.model.AccountResponse;
import com.banking.account.model.Account;
import org.springframework.stereotype.Component;

/**
 * Mapper to convert between Account entity and DTOs
 */
@Component
public class AccountMapper {

    /**
     * Convert AccountRequest DTO to Account entity
     * Used when creating or updating an account
     *
     * @param request AccountRequest DTO from API
     * @return Account entity for database
     */
    public Account toEntity(AccountRequest request) {
        if (request == null) {
            return null;
        }

        return Account.builder()
                .accountNumber(request.getAccountNumber())
                .accountType(request.getAccountType() != null ? request.getAccountType().name() : null)
                .initialBalance(request.getInitialBalance())
                .currentBalance(request.getInitialBalance()) // Initially same as initial balance
                .status(request.getStatus() != null ? request.getStatus() : true)
                .customerId(request.getCustomerId())
                .build();
    }

    /**
     * Convert Account entity to AccountResponse DTO
     * Used when returning data to the client
     *
     * @param account Account entity from database
     * @return AccountResponse DTO for API
     */
    public AccountResponse toResponse(Account account) {
        if (account == null) {
            return null;
        }

        AccountResponse response = new AccountResponse();
        response.setAccountId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountType(account.getAccountType());
        response.setInitialBalance(account.getInitialBalance());
        response.setCurrentBalance(account.getCurrentBalance());
        response.setStatus(account.getStatus());
        response.setCustomerId(account.getCustomerId());

        return response;
    }
}
