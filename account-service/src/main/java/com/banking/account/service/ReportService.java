package com.banking.account.service;

import com.banking.account.infrastructure.adapter.rest.generated.model.AccountStatementResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * Service for generating account statement reports.
 */
public interface ReportService {

    /**
     * Generate account statement report for a customer.
     *
     * @param customerId customer identifier
     * @param startDate  start date (inclusive)
     * @param endDate    end date (inclusive)
     * @return Mono of AccountStatementResponse with complete report
     */
    Mono<AccountStatementResponse> generateAccountStatement(
            Long customerId,
            LocalDate startDate,
            LocalDate endDate);
}
