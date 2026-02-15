package com.banking.account.service;

import com.banking.account.client.CustomerServiceClient;
import com.banking.account.infrastructure.adapter.rest.generated.model.AccountStatementDetail;
import com.banking.account.infrastructure.adapter.rest.generated.model.AccountStatementResponse;
import com.banking.account.infrastructure.adapter.rest.generated.model.CustomerResponse;
import com.banking.account.mapper.ReportMapper;
import com.banking.account.model.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

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
     */
    @Override
    public Mono<AccountStatementResponse> generateAccountStatement(Long customerId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating account statement - CustomerId: {}, Period: {} to {}",
                customerId, startDate, endDate);

        // Get customer info from customer-service
        Mono<String> customerNameMono = customerServiceClient.getCustomerById(customerId)
                .map(CustomerResponse::getName)
                .defaultIfEmpty("Unknown Customer");

        //Get all accounts for customer and process each
        Mono<List<AccountStatementDetail>> accountDetailsMono = accountService
                .getAccountsByCustomerId(customerId)
                .flatMap(account -> buildAccountDetail(account, startDate, endDate))
                .collectList();

        // Combine customer info + account details into final report
        return Mono.zip(customerNameMono, accountDetailsMono)
                .map(tuple -> {
                    String customerName = tuple.getT1();
                    List<AccountStatementDetail> accountDetails = tuple.getT2();

                    return reportMapper.toAccountStatementResponse(
                            customerId,
                            customerName,
                            startDate,
                            endDate,
                            accountDetails
                    );
                })
                .doOnSuccess(report ->
                        log.info("Account statement generated successfully - CustomerId: {}, Accounts: {}",
                                customerId, report.getAccounts().size()))
                .doOnError(error ->
                        log.error("Error generating account statement for customer {}: {}",
                                customerId, error.getMessage()));
    }

    /**
     * Build account detail with movements for date range
     *
     * @param account   account entity
     * @param startDate start date
     * @param endDate   end date
     * @return Mono of AccountStatementDetail
     */
    private Mono<AccountStatementDetail> buildAccountDetail(
            Account account,
            LocalDate startDate,
            LocalDate endDate) {

        log.debug("Building statement detail for account: {}", account.getAccountNumber());

        return movementService
                .getMovementsByAccountAndDateRange(account.getId(), startDate, endDate)
                .collectList()
                .map(movements -> {
                    log.debug("Account {} has {} movements in period",
                            account.getAccountNumber(), movements.size());
                    return reportMapper.toAccountStatementDetail(account, movements);
                });
    }
}
