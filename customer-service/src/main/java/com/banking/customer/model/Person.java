package com.banking.customer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Person base class containing common personal information.
 * Acts as a superclass for Customer (and potential future entities like
 * Employee).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Person {

    /**
     * Full name of the person.
     */
    protected String name;

    /**
     * Gender (e.g., "Male", "Female", or specific enum code).
     */
    protected String gender;

    /**
     * Age of the person.
     */
    protected Integer age;

    /**
     * Unique personal identification (e.g., ID Card, SSN).
     * This is a critical business key.
     */
    protected String identification;

    /**
     * Physical address.
     */
    protected String address;

    /**
     * Contact phone number.
     */
    protected String phone;
}