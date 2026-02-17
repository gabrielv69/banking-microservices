package com.banking.account.mapper;

import com.banking.account.infrastructure.adapter.rest.generated.model.AccountStatementDetail;
import com.banking.account.infrastructure.adapter.rest.generated.model.AccountStatementResponse;
import com.banking.account.infrastructure.adapter.rest.generated.model.MovementResponse;
import com.banking.account.model.Account;
import com.banking.account.model.Movement;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for Account Statement Reports (F4)
 * Converts accounts and movements into report format
 */
@Component
public class ReportMapper {

    private final MovementMapper movementMapper;

    public ReportMapper(MovementMapper movementMapper) {
        this.movementMapper = movementMapper;
    }

    /**
     * Create an AccountStatementDetail from an Account and its Movements
     *
     * @param account Account entity
     * @param movements List of movements for this account
     * @return AccountStatementDetail for the report
     */
    public AccountStatementDetail toAccountStatementDetail(Account account, List<Movement> movements) {
        if (account == null) {
            return null;
        }

        AccountStatementDetail detail = new AccountStatementDetail();
        detail.setAccountId(account.getId());
        detail.setAccountNumber(account.getAccountNumber());
        detail.setAccountType(account.getAccountType());
        detail.setCurrentBalance(account.getCurrentBalance());

        // Convert movements to DTOs
        if (movements != null) {
            List<MovementResponse> movementResponses = movements.stream()
                    .map(movementMapper::toResponse)
                    .collect(Collectors.toList());
            detail.setMovements(movementResponses);
        }

        return detail;
    }

    /**
     * Create the complete AccountStatementResponse
     *
     * @param customerId Customer ID
     * @param customerName Customer name (from customer-service)
     * @param startDate Start date of the report
     * @param endDate End date of the report
     * @param accountDetails List of account details with movements
     * @return Complete AccountStatementResponse
     */
    public AccountStatementResponse toAccountStatementResponse(
            Long customerId,
            String customerName,
            LocalDate startDate,
            LocalDate endDate,
            List<AccountStatementDetail> accountDetails) {

        AccountStatementResponse response = new AccountStatementResponse();
        response.setCustomerId(customerId);
        response.setCustomerName(customerName);
        response.setStartDate(startDate);
        response.setEndDate(endDate);
        response.setAccounts(accountDetails);

        return response;
    }
}