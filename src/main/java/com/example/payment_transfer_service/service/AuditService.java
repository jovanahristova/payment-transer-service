package com.example.payment_transfer_service.service;

import com.example.payment_transfer_service.entity.*;
import com.example.payment_transfer_service.repository.TransactionAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final TransactionAuditRepository auditRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSuccessfulTransfer(Transaction transaction,
                                         BigDecimal sourceBalanceBefore, BigDecimal sourceBalanceAfter,
                                         BigDecimal destBalanceBefore, BigDecimal destBalanceAfter) {
        try {
            TransactionAudit audit = new TransactionAudit();
            audit.setTransactionId(transaction.getId());
            audit.setUserId(transaction.getUserId());
            audit.setSourceAccountId(transaction.getSourceAccountId());
            audit.setDestinationAccountId(transaction.getDestinationAccountId());
            audit.setAmount(transaction.getAmount());
            audit.setSourceBalanceBefore(sourceBalanceBefore);
            audit.setSourceBalanceAfter(sourceBalanceAfter);
            audit.setDestBalanceBefore(destBalanceBefore);
            audit.setDestBalanceAfter(destBalanceAfter);
            audit.setStatus(transaction.getStatus());
            audit.setDescription(transaction.getDescription());
            audit.setReference(transaction.getReference());
            audit.setSuccess(true);

            auditRepository.save(audit);

            log.info("Transfer audit recorded: {} - Amount: {} - Status: SUCCESS",
                    transaction.getId(), transaction.getAmount());

        } catch (Exception e) {
            log.error("Failed to record transfer audit for transaction: {}", transaction.getId(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedTransfer(String userId, String sourceAccountId, String destinationAccountId,
                                     BigDecimal amount, String errorMessage) {
        try {
            TransactionAudit audit = new TransactionAudit();
            audit.setTransactionId("FAILED_" + System.currentTimeMillis());
            audit.setUserId(userId);
            audit.setSourceAccountId(sourceAccountId);
            audit.setDestinationAccountId(destinationAccountId);
            audit.setAmount(amount);
            audit.setStatus(TransactionStatus.FAILED);
            audit.setSuccess(false);
            audit.setErrorMessage(errorMessage);

            auditRepository.save(audit);

            log.warn("Failed transfer audit recorded: User {} - Amount: {} - Error: {}",
                    userId, amount, errorMessage);

        } catch (Exception e) {
            log.error("Failed to record failed transfer audit", e);
        }
    }
}
