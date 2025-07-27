package com.example.payment_transfer_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "source_account_id", nullable = false)
    private String sourceAccountId;

    @Column(name = "destination_account_id", nullable = false)
    private String destinationAccountId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "source_balance_before", precision = 19, scale = 2)
    private BigDecimal sourceBalanceBefore;

    @Column(name = "source_balance_after", precision = 19, scale = 2)
    private BigDecimal sourceBalanceAfter;

    @Column(name = "dest_balance_before", precision = 19, scale = 2)
    private BigDecimal destBalanceBefore;

    @Column(name = "dest_balance_after", precision = 19, scale = 2)
    private BigDecimal destBalanceAfter;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(name = "description")
    private String description;

    @Column(name = "reference")
    private String reference;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "success")
    private boolean success;

    @Column(name = "error_message")
    private String errorMessage;
}
