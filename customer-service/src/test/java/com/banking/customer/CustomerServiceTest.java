package com.banking.customer;

import com.banking.customer.exception.CustomerAlreadyExistsException;
import com.banking.customer.exception.CustomerNotFoundException;
import com.banking.customer.model.Customer;
import com.banking.customer.repository.CustomerRepository;
import com.banking.customer.service.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CustomerService.
 * Tests CRUD operations and business validation rules.
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(1L)
                .name("Jose Lema")
                .gender("MALE")
                .identification("1234567890")
                .address("Otavalo sn y principal")
                .phone("098254785")
                .password("1234")
                .status(true)
                .build();
    }

    @Test
    @DisplayName("Get customer by ID - should return customer")
    void testGetCustomerById_Success() {
        when(customerRepository.findById(anyLong())).thenReturn(Mono.just(testCustomer));

        StepVerifier.create(customerService.getCustomerById(1L))
                .expectNextMatches(customer -> customer.getName().equals("Jose Lema") &&
                        customer.getIdentification().equals("1234567890"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Get customer by ID - not found should throw exception")
    void testGetCustomerById_NotFound() {
        when(customerRepository.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(customerService.getCustomerById(999L))
                .expectErrorMatches(error -> error instanceof CustomerNotFoundException &&
                        error.getMessage().contains("999"))
                .verify();
    }

    @Test
    @DisplayName("Get all customers - should return flux of customers")
    void testGetAllCustomers_Success() {
        Customer customer2 = Customer.builder()
                .id(2L)
                .name("Marianela Montalvo")
                .gender("FEMALE")
                .identification("0987654321")
                .build();

        when(customerRepository.findAll()).thenReturn(Flux.just(testCustomer, customer2));

        StepVerifier.create(customerService.getAllCustomers())
                .expectNextMatches(customer -> customer.getName().equals("Jose Lema"))
                .expectNextMatches(customer -> customer.getName().equals("Marianela Montalvo"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Create customer - should return created customer")
    void testCreateCustomer_Success() {
        Customer newCustomer = Customer.builder()
                .name("New Customer")
                .gender("MALE")
                .identification("1111111111")
                .address("New Address")
                .phone("0999999999")
                .password("pass")
                .build();

        Customer savedCustomer = Customer.builder()
                .id(3L)
                .name("New Customer")
                .gender("MALE")
                .identification("1111111111")
                .address("New Address")
                .phone("0999999999")
                .password("pass")
                .status(true)
                .build();

        when(customerRepository.existsByIdentification(anyString())).thenReturn(Mono.just(false));
        when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(savedCustomer));

        StepVerifier.create(customerService.createCustomer(newCustomer))
                .expectNextMatches(customer -> customer.getId() == 3L &&
                        customer.getName().equals("New Customer") &&
                        customer.getStatus())
                .verifyComplete();
    }

    @Test
    @DisplayName("Create customer - duplicate identification should throw exception")
    void testCreateCustomer_DuplicateIdentification() {
        when(customerRepository.existsByIdentification(anyString())).thenReturn(Mono.just(true));

        StepVerifier.create(customerService.createCustomer(testCustomer))
                .expectErrorMatches(error -> error instanceof CustomerAlreadyExistsException &&
                        error.getMessage().contains("1234567890"))
                .verify();
    }

    @Test
    @DisplayName("Update customer - should return updated customer")
    void testUpdateCustomer_Success() {
        Customer updatedCustomer = Customer.builder()
                .id(1L)
                .name("Jose Lema Updated")
                .gender("MALE")
                .identification("1234567890")
                .address("New Address")
                .phone("098254785")
                .password("newpass")
                .status(true)
                .build();

        when(customerRepository.findById(anyLong())).thenReturn(Mono.just(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(updatedCustomer));

        StepVerifier.create(customerService.updateCustomer(1L, updatedCustomer))
                .expectNextMatches(customer -> customer.getName().equals("Jose Lema Updated") &&
                        customer.getAddress().equals("New Address"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Update customer - not found should throw exception")
    void testUpdateCustomer_NotFound() {
        when(customerRepository.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(customerService.updateCustomer(999L, testCustomer))
                .expectErrorMatches(error -> error instanceof CustomerNotFoundException &&
                        error.getMessage().contains("999"))
                .verify();
    }

    @Test
    @DisplayName("Delete customer - should complete successfully")
    void testDeleteCustomer_Success() {
        when(customerRepository.findById(anyLong())).thenReturn(Mono.just(testCustomer));
        when(customerRepository.delete(any(Customer.class))).thenReturn(Mono.empty());

        StepVerifier.create(customerService.deleteCustomer(1L))
                .verifyComplete();
    }

    @Test
    @DisplayName("Delete customer - not found should throw exception")
    void testDeleteCustomer_NotFound() {
        when(customerRepository.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(customerService.deleteCustomer(999L))
                .expectErrorMatches(error -> error instanceof CustomerNotFoundException &&
                        error.getMessage().contains("999"))
                .verify();
    }
}
