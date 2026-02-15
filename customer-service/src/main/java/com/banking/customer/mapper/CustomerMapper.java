package com.banking.customer.mapper;

import com.banking.customer.infrastructure.adapter.rest.generated.model.CustomerRequest;
import com.banking.customer.infrastructure.adapter.rest.generated.model.CustomerResponse;
import com.banking.customer.model.Customer;
import org.springframework.stereotype.Component;

/**
 * Mapper to convert between Customer entity and DTOs
 * Manual mapping to avoid heavy dependencies like MapStruct
 */
@Component
public class CustomerMapper {

    /**
     * Convert CustomerRequest DTO to Customer entity
     * Used when creating or updating a customer
     *
     * @param request CustomerRequest DTO from API
     * @return Customer entity for database
     */
    public Customer toEntity(CustomerRequest request) {
        if (request == null) {
            return null;
        }

        return Customer.builder()
                .name(request.getName())
                .gender(request.getGender() != null ? request.getGender().name() : null)
                .identification(request.getIdentification())
                .address(request.getAddress())
                .phone(request.getPhone())
                .password(request.getPassword())
                .status(request.getStatus() != null ? request.getStatus() : true)
                .build();
    }


    /**
     * Convert Customer entity to CustomerResponse DTO
     * Used when returning data to the client
     *
     * @param customer Customer entity from database
     * @return CustomerResponse DTO for API
     */
    public CustomerResponse toResponse(Customer customer) {
        if (customer == null) {
            return null;
        }

        CustomerResponse response = new CustomerResponse();
        response.setCustomerId(customer.getId());
        response.setName(customer.getName());
        response.setGender(customer.getGender());
        response.setIdentification(customer.getIdentification());
        response.setAddress(customer.getAddress());
        response.setPhone(customer.getPhone());
        response.setStatus(customer.getStatus());

        return response;
    }

    /**
     * Update existing Customer entity with data from CustomerRequest
     * Used in update operations to preserve the ID
     *
     * @param entity Existing customer entity
     * @param request Updated customer data
     * @return Updated customer entity
     */
    public Customer updateEntityFromRequest(Customer entity, CustomerRequest request) {
        if (entity == null || request == null) {
            return entity;
        }

        entity.setName(request.getName());
        entity.setGender(request.getGender() != null ? request.getGender().name() : null);
        entity.setIdentification(request.getIdentification());
        entity.setAddress(request.getAddress());
        entity.setPhone(request.getPhone());
        entity.setPassword(request.getPassword());
        entity.setStatus(request.getStatus() != null ? request.getStatus() : true);

        return entity;
    }

}
