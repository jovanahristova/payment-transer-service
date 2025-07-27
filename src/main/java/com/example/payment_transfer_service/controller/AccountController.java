package com.example.payment_transfer_service.controller;

import com.example.payment_transfer_service.dto.*;
import com.example.payment_transfer_service.entity.AccountStatus;
import com.example.payment_transfer_service.service.AccountService;
import com.example.payment_transfer_service.service.PaymentTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final PaymentTransferService paymentTransferService;

    @PostMapping
    public ResponseEntity<AccountSummary> createAccount(@Valid @RequestBody AccountCreationRequest request) {
        AccountSummary account = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountSummary>> getUserAccounts(@PathVariable String userId) {
        List<AccountSummary> accounts = accountService.getUserAccounts(userId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountSummary> getAccount(
            @PathVariable String accountId,
            @RequestParam String userId) {
        AccountSummary account = accountService.getAccountById(accountId, userId);
        return ResponseEntity.ok(account);
    }

    @PutMapping("/{accountId}/status")
    public ResponseEntity<AccountSummary> updateAccountStatus(
            @PathVariable String accountId,
            @RequestParam String userId,
            @RequestParam AccountStatus status) {
        AccountSummary account = accountService.updateAccountStatus(accountId, userId, status);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<List<UserTransactionHistory>> getAccountTransactions(
            @PathVariable String accountId,
            @RequestParam String userId) {
        List<UserTransactionHistory> transactions =
                paymentTransferService.getAccountTransactionHistory(accountId, userId);
        return ResponseEntity.ok(transactions);
    }

}
