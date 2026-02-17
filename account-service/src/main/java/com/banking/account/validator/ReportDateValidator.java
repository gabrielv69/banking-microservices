package com.banking.account.validator;

import com.banking.account.exception.InvalidDateRangeException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Validator for report date ranges
 * Ensures date parameters are valid and reasonable
 */
@Component
public class ReportDateValidator {

    private static final long MAX_DAYS_RANGE = 365; // 1 year max

    /**
     * Validates date range for reports
     *
     * @param startDate Start date
     * @param endDate End date
     * @throws InvalidDateRangeException if dates are invalid
     */
    public void validateDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();

        // 1. Validate startDate is not after endDate
        if (startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException(
                    String.format("Start date (%s) cannot be after end date (%s)",
                            startDate, endDate));
        }

        // 2. Validate endDate is not in the future
        if (endDate.isAfter(today)) {
            throw new InvalidDateRangeException(
                    String.format("End date (%s) cannot be in the future (today: %s)",
                            endDate, today));
        }

        // 3. Validate range is not too large
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > MAX_DAYS_RANGE) {
            throw new InvalidDateRangeException(
                    String.format("Date range is too large (%d days). Maximum allowed: %d days",
                            daysBetween, MAX_DAYS_RANGE));
        }
    }
}