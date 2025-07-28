package com.example.payment_transfer_service.controller;

import com.example.payment_transfer_service.entity.TransactionAudit;
import com.example.payment_transfer_service.repository.TransactionAuditRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")  // Now requires JWT token
@Tag(name = "Audit Operations", description = "...")
public class AuditController {

    private final TransactionAuditRepository auditRepository;

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<List<TransactionAudit>> getTransactionAudit(@PathVariable String transactionId) {
        List<TransactionAudit> audits = auditRepository.findByTransactionIdOrderByCreatedAtDesc(transactionId);
        return ResponseEntity.ok(audits);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<TransactionAudit>> getUserAudits(
            @PathVariable String userId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TransactionAudit> audits = auditRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return ResponseEntity.ok(audits);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionAudit>> getAccountAudits(@PathVariable String accountId) {
        List<TransactionAudit> audits = auditRepository.findByAccountId(accountId);
        return ResponseEntity.ok(audits);
    }

    @GetMapping("/failed")
    public ResponseEntity<List<TransactionAudit>> getFailedTransfers() {
        List<TransactionAudit> audits = auditRepository.findBySuccessOrderByCreatedAtDesc(false);
        return ResponseEntity.ok(audits);
    }

    @GetMapping("/daily")
    public ResponseEntity<List<TransactionAudit>> getDailyAudits(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<TransactionAudit> audits = auditRepository.findByDateRange(startOfDay, endOfDay);
        return ResponseEntity.ok(audits);
    }
}
