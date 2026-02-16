package com.banking.account.controller;

import com.banking.account.constants.AccountMessages;
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
 * REST Controller for Reports.
 * Implements ReportsApi from OpenAPI specification.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class ReportController implements ReportsApi {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public Mono<ResponseEntity<AccountStatementResponse>> generateAccountStatement(
            Long customerId, LocalDate startDate, LocalDate endDate, ServerWebExchange exchange) {
        log.info(AccountMessages.LOG_REPORT_GENERATE, customerId, startDate, endDate);

        return reportService.generateAccountStatement(customerId, startDate, endDate)
                .map(ResponseEntity::ok);
    }
}
