package com.banking.customer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Customer Entity.
 * Extends basic Person information with banking-specific fields.
 * Mapped to the 'customer' table.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("customer")
public class Customer extends Person {

    /**
     * Unique Customer ID (Primary Key).
     */
    @Id
    private Long id;

    /**
     * Password or PIN for authentication.
     */
    @Column("password")
    private String password;

    /**
     * Account status (Active/Inactive).
     */
    @Column("status")
    private Boolean status;

}