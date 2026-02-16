package com.banking.account.service;

import com.banking.account.client.CustomerServiceClient;
import com.banking.account.constants.AccountMessages;
import com.banking.account.exception.CustomerNotFoundException;
import com.banking.account.infrastructure.adapter.rest.generated.model.AccountStatementDetail;
import com.banking.account.infrastructure.adapter.rest.generated.model.AccountStatementResponse;
import com.banking.account.infrastructure.adapter.rest.generated.model.CustomerResponse;
import com.banking.account.mapper.ReportMapper;
import com.banking.account.model.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * Implementation of ReportService
 * Responsibilities:
 * - Orchestrate calls to multiple services
 * - Aggregate data from accounts and movements
 * - Enrich with customer information from customer-service
 */
@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    private final AccountService accountService;
    private final MovementService movementService;
    private final CustomerServiceClient customerServiceClient;
    private final ReportMapper reportMapper;

    public ReportServiceImpl(AccountService accountService,
                             MovementService movementService,
                             CustomerServiceClient customerServiceClient,
                             ReportMapper reportMapper) {
        this.accountService = accountService;
        this.movementService = movementService;
        this.customerServiceClient = customerServiceClient;
        this.reportMapper = reportMapper;
    }

    /**
     * {@inheritDoc}
     * <p>
     * F4: Generate account statement report
     * Validates customer exists FIRST (Fail Fast principle)
     * Then generates report with accounts and movements
     */
    @Override
    public Mono<AccountStatementResponse> generateAccountStatement(
            Long customerId,
            LocalDate startDate,
            LocalDate endDate) {

        log.info(AccountMessages.LOG_REPORT_GENERATE,
                customerId, startDate, endDate);

        // Validate customer exists FIRST (Fail Fast)
        return validateCustomerExists(customerId)
                .flatMap(customerName -> generateReportForValidCustomer(
                        customerId,
                        customerName,
                        startDate,
                        endDate
                ))
                .doOnSuccess(report -> log.info(AccountMessages.LOG_REPORT_GENERATED_DETAIL,
                        customerId, report.getAccounts().size()))
                .doOnError(error -> log.error(AccountMessages.LOG_REPORT_ERROR_DETAIL,
                        customerId, error.getMessage()));
    }

    /**
     * Validates that customer exists in customer-service
     * Throws CustomerNotFoundException if not found (404)
     *
     * @param customerId Customer ID to validate
     * @return Mono with customer name
     */
    private Mono<String> validateCustomerExists(Long customerId) {
        return customerServiceClient.getCustomerById(customerId)
                .map(CustomerResponse::getName)
                .switchIfEmpty(Mono.error(
                        new CustomerNotFoundException(
                                String.format(AccountMessages.CUSTOMER_NOT_FOUND, customerId)
                        )
                ))
                .doOnNext(name -> log.debug("Customer validated: {} - {}", customerId, name));
    }

    /**
     * Generates report for a validated customer
     * Single Responsibility: Only generate report, not validate
     *
     * @param customerId   Customer ID
     * @param customerName Customer name (already validated)
     * @param startDate    Report start date
     * @param endDate      Report end date
     * @return Complete AccountStatementResponse
     */
    private Mono<AccountStatementResponse> generateReportForValidCustomer(
            Long customerId,
            String customerName,
            LocalDate startDate,
            LocalDate endDate) {

        return accountService.getAccountsByCustomerId(customerId)
                .flatMap(account -> buildAccountDetail(account, startDate, endDate))
                .collectList()
                .map(accountDetails -> reportMapper.toAccountStatementResponse(
                        customerId,
                        customerName,
                        startDate,
                        endDate,
                        accountDetails
                ));
    }

    /**
     * Builds detailed information for a single account including movements
     *
     * @param account   Account entity
     * @param startDate Filter start date
     * @param endDate   Filter end date
     * @return AccountStatementDetail with account info and movements
     */
    private Mono<AccountStatementDetail> buildAccountDetail(
            Account account,
            LocalDate startDate,
            LocalDate endDate) {

        return movementService.getMovementsByAccountAndDateRange(
                        account.getId(),
                        startDate,
                        endDate
                )
                .collectList()
                .map(movements -> reportMapper.toAccountStatementDetail(account, movements));
    }
}
