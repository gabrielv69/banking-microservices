package com.banking.account.service;
import com.banking.account.infrastructure.adapter.rest.generated.model.AccountStatementResponse;
import reactor.core.publisher.Mono;
import java.time.LocalDate;


/**
 * Service for generating account statement reports

 */
public interface ReportService {

    /**
     * Generate account statement report for a customer
     * Flow:
     * 1. Validate customer exists (call customer-service)
     * 2. Get all accounts for the customer
     * 3. For each account, get movements in date range
     * 4. Build report with accounts + movements
     *
     * @param customerId customer identifier
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return Mono of AccountStatementResponse with complete report
     */
    Mono<AccountStatementResponse> generateAccountStatement(
            Long customerId,
            LocalDate startDate,
            LocalDate endDate
    );
}






