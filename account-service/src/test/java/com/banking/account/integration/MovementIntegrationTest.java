package com.banking.account.integration;

import com.banking.account.infrastructure.adapter.rest.generated.model.MovementRequest;
import com.banking.account.model.Account;
import com.banking.account.model.Movement;
import com.banking.account.repository.AccountRepository;
import com.banking.account.repository.MovementRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * Integration test for Movement functionality
 * Tests the complete flow: Controller → Service → Repository → Database
 *
 * Requirement F6: Integration test
 * Validates F2 and F3: Movement registration and balance validation
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class MovementIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MovementRepository movementRepository;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        movementRepository.deleteAll().block();
        accountRepository.deleteAll().block();

        // Create test account
        testAccount = Account.builder()
                .accountNumber("TEST123456")
                .accountType("SAVINGS")
                .initialBalance(new BigDecimal("2000.00"))
                .currentBalance(new BigDecimal("2000.00"))
                .status(true)
                .customerId(1L)
                .build();

        testAccount = accountRepository.save(testAccount).block();
    }

    @AfterEach
    void tearDown() {
        // Clean database after each test
        movementRepository.deleteAll().block();
        accountRepository.deleteAll().block();
    }

    /**
     * F2 Integration Test: Register DEBIT movement
     * Validates complete flow from HTTP request to database
     */
    @Test
    @DisplayName("F2 Integration: Register DEBIT movement - should update balance in database")
    void testRegisterDebitMovement_Integration() {
        MovementRequest request = new MovementRequest();
        request.setAccountId(testAccount.getId());
        request.setMovementType(MovementRequest.MovementTypeEnum.DEBIT);
        request.setValue(new BigDecimal("575.00"));

        // Act: Make HTTP POST request
        webTestClient.post()
                .uri("/api/v1/movements")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), MovementRequest.class)
                .exchange()

                // Assert: HTTP response
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.movementId").exists()
                .jsonPath("$.movementType").isEqualTo("DEBIT")
                .jsonPath("$.value").isEqualTo(575.00)
                .jsonPath("$.balance").isEqualTo(1425.00)  // 2000 - 575
                .jsonPath("$.accountId").isEqualTo(testAccount.getId());

        //Verify movement was saved
        Movement savedMovement = movementRepository.findByAccountId(testAccount.getId())
                .blockFirst();

        assertThat(savedMovement).isNotNull();
        assertThat(savedMovement.getMovementType()).isEqualTo("DEBIT");
        assertThat(savedMovement.getValue()).isEqualByComparingTo(new BigDecimal("575.00"));
        assertThat(savedMovement.getBalance()).isEqualByComparingTo(new BigDecimal("1425.00"));

        //Verify account balance was updated
        Account updatedAccount = accountRepository.findById(testAccount.getId()).block();
        assertThat(updatedAccount).isNotNull();
        assertThat(updatedAccount.getCurrentBalance())
                .isEqualByComparingTo(new BigDecimal("1425.00"));
    }


    /**
     * F2 Integration Test: Register CREDIT movement
     * Validates complete flow for credit transactions
     */
    @Test
    @DisplayName("F2 Integration: Register CREDIT movement - should increase balance")
    void testRegisterCreditMovement_Integration() {
        MovementRequest request = new MovementRequest();
        request.setAccountId(testAccount.getId());
        request.setMovementType(MovementRequest.MovementTypeEnum.CREDIT);
        request.setValue(new BigDecimal("1000.00"));

        // Act: Make HTTP POST request
        webTestClient.post()
                .uri("/api/v1/movements")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), MovementRequest.class)
                .exchange()

                // Assert: HTTP response
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.movementType").isEqualTo("CREDIT")
                .jsonPath("$.value").isEqualTo(1000.00)
                .jsonPath("$.balance").isEqualTo(3000.00);  // 2000 + 1000

        // Assert: Database state
        Account updatedAccount = accountRepository.findById(testAccount.getId()).block();
        Assertions.assertNotNull(updatedAccount);
        assertThat(updatedAccount.getCurrentBalance())
                .isEqualByComparingTo(new BigDecimal("3000.00"));
    }

    /**
     * F3 Integration Test: Insufficient balance validation
     * Validates "Saldo no disponible" error handling
     */
    @Test
    @DisplayName("F3 Integration: DEBIT with insufficient balance - should return 'Saldo no disponible'")
    void testRegisterDebitMovement_InsufficientBalance_Integration() {
        MovementRequest request = new MovementRequest();
        request.setAccountId(testAccount.getId());
        request.setMovementType(MovementRequest.MovementTypeEnum.DEBIT);
        request.setValue(new BigDecimal("3000.00"));  // More than balance (2000)

        // Act & Assert: Should return 400 Bad Request
        webTestClient.post()
                .uri("/api/v1/movements")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), MovementRequest.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Saldo no disponible");

        //  No movement should be saved
        Long movementCount = movementRepository.findByAccountId(testAccount.getId())
                .count()
                .block();
        assertThat(movementCount).isEqualTo(0);

        // Account balance should remain unchanged
        Account unchangedAccount = accountRepository.findById(testAccount.getId()).block();
        Assertions.assertNotNull(unchangedAccount);
        assertThat(unchangedAccount.getCurrentBalance())
                .isEqualByComparingTo(new BigDecimal("2000.00"));
    }

    /**
     * F2 Integration Test: Invalid movement value
     * Validates that movements with value <= 0 are rejected
     */
    @Test
    @DisplayName("F2 Integration: Movement with zero value - should fail validation")
    void testRegisterMovement_ZeroValue_Integration() {
        MovementRequest request = new MovementRequest();
        request.setAccountId(testAccount.getId());
        request.setMovementType(MovementRequest.MovementTypeEnum.DEBIT);
        request.setValue(BigDecimal.ZERO);

        // Should return 400 Bad Request
        webTestClient.post()
                .uri("/api/v1/movements")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), MovementRequest.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").value(message ->
                        assertThat(message.toString()).contains("debe ser mayor que o igual a 0.01"));

        //No movement saved
        Long movementCount = movementRepository.findByAccountId(testAccount.getId())
                .count()
                .block();
        assertThat(movementCount).isEqualTo(0);
    }

}
