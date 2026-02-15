package com.banking.customer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Person base class containing common personal information
 * As per requirement: "Implementar la clase persona con los siguientes datos:
 * nombre, genero, identificación, dirección, teléfono"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Person {

    protected String name;
    protected String gender;
    protected String identification;
    protected String address;
    protected String phone;
}