package com.banking.account.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("movement")
public class Movement {

    @Id
    private Long id;

    @Column("date")
    private LocalDateTime date;

    @Column("movement_type")
    private String movementType;

    @Column("value")
    private BigDecimal value;

    @Column("balance")
    private BigDecimal balance;

    @Column("account_id")
    private Long accountId;
}