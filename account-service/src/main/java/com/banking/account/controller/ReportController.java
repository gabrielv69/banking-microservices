package com.banking.account.controller;

import com.banking.account.infrastructure.adapter.rest.generated.ReportsApi;
import com.banking.account.infrastructure.adapter.rest.generated.model.AccountStatementResponse;
import com.banking.account.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * REST Controller for Reports
 *
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class ReportController implements ReportsApi {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Generate account statement report
     *
     * @param customerId customer identifier
     * @param startDate start date (format: yyyy-MM-dd)
     * @param endDate end date (format: yyyy-MM-dd)
     * @param exchange server exchange
     * @return ResponseEntity with account statement report
     */
    @Override
    public Mono<ResponseEntity<AccountStatementResponse>> generateAccountStatement(Long customerId, LocalDate startDate, LocalDate endDate, ServerWebExchange exchange) {
        log.info("GET /api/v1/reports/{} - Generating account statement (startDate: {}, endDate: {})",
                customerId, startDate, endDate);

        return reportService.generateAccountStatement(customerId, startDate, endDate)
                .map(ResponseEntity::ok)
                .doOnSuccess(response ->
                        log.info("Account statement generated successfully for customer: {}", customerId))
                .doOnError(error ->
                        log.error("Error generating account statement for customer {}: {}",
                                customerId, error.getMessage()));
    }
}
