package com.banking.account.mapper;

import com.banking.account.infrastructure.adapter.rest.generated.model.MovementRequest;
import com.banking.account.infrastructure.adapter.rest.generated.model.MovementResponse;
import com.banking.account.model.Movement;
import com.banking.account.model.MovementType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Mapper to convert between Movement entity and DTOs
 */
@Component
public class MovementMapper {

    /**
     * Convert MovementRequest DTO to Movement entity
     * Used when creating a new movement
     *
     * @param request MovementRequest DTO from API
     * @return Movement entity for database
     */
    public Movement toEntity(MovementRequest request) {
        if (request == null) {
            return null;
        }

        return Movement.builder()
                .accountId(request.getAccountId())
                .accountNumber(request.getAccountNumber())
                .movementType(request.getMovementType() != null ? request.getMovementType().name() : null)
                .value(request.getValue())
                .date(LocalDateTime.now())
                .build();
    }

    /**
     * Convert Movement entity to MovementResponse DTO
     * Used when returning data to the client
     *
     * @param movement Movement entity from database
     * @return MovementResponse DTO for API
     */
    public MovementResponse toResponse(Movement movement) {
        if (movement == null) {
            return null;
        }

        MovementResponse response = new MovementResponse();
        response.setMovementId(movement.getId());
        response.setDate(movement.getDate() != null ? movement.getDate().atOffset(ZoneOffset.UTC) : null);
        response.setMovementType(movement.getMovementType());
        response.setValue(movement.getValue());
        response.setBalance(movement.getBalance());
        response.setInitialBalance(reverseMovement(movement));
        response.setAccountId(movement.getAccountId());

        return response;
    }


    /**
     * Reverses a movement to obtain the balance BEFORE it was applied.
     * @param movement Any movement with balance and value set
     * @return Balance before this specific movement was applied
     */
    private BigDecimal reverseMovement(Movement movement) {
        BigDecimal balanceAfter = movement.getBalance();
        BigDecimal value = movement.getValue();

        return MovementType.CREDIT.name().equalsIgnoreCase(movement.getMovementType())
                ? balanceAfter.subtract(value)   // CREDIT: undo addition
                : balanceAfter.add(value);        // DEBIT:  undo subtraction
    }
}