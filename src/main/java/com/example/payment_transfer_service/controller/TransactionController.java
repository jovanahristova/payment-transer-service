package com.example.payment_transfer_service.controller;

import com.example.payment_transfer_service.dto.*;
import com.example.payment_transfer_service.entity.Transaction;
import com.example.payment_transfer_service.service.PaymentTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final PaymentTransferService paymentTransferService;

    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable String transactionId) {
        try {
            Transaction transaction = paymentTransferService.getTransactionById(transactionId);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserTransactionHistory>> getUserTransactions(@PathVariable String userId) {
        List<UserTransactionHistory> transactions = paymentTransferService.getUserTransactionHistory(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<UserTransactionHistory>> getAccountTransactions(
            @PathVariable String accountId,
            @RequestParam String userId) {
        List<UserTransactionHistory> transactions =
                paymentTransferService.getAccountTransactionHistory(accountId, userId);
        return ResponseEntity.ok(transactions);
    }
}
