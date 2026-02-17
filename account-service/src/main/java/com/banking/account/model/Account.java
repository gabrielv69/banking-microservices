package com.banking.account.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

/**
 * Account Entity.
 * Represents a banking account stored in the 'account' table.
 * <p>
 * Key Constraints:
 * - 'accountNumber' must be unique.
 * - 'customerId' links to the Customer in the customer-service (logical FK).
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("account")
public class Account {

    /**
     * Internal unique identifier (Primary Key).
     */
    @Id
    private Long id;

    /**
     * Public unique account number (Business Key).
     * Cannot be changed after creation.
     */
    @Column("account_number")
    private String accountNumber;

    /**
     * Type of account (SAVINGS, CHECKING).
     * Stored as String database-side for readout definition, mapped to Enum in
     * logic.
     */
    @Column("account_type")
    private String accountType;

    /**
     * Balance at the time of account creation.
     */
    @Column("initial_balance")
    private BigDecimal initialBalance;

    /**
     * Current available balance.
     * Updated transactionally by movements.
     */
    @Column("current_balance")
    private BigDecimal currentBalance;

    /**
     * Account status (Active/Inactive).
     */
    @Column("status")
    private Boolean status;

    /**
     * ID of the customer who owns this account.
     */
    @Column("customer_id")
    private Long customerId;

}