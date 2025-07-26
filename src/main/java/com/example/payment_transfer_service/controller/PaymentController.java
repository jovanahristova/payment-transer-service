package com.example.payment_transfer_service.controller;

import com.example.payment_transfer_service.dto.TransferRequest;
import com.example.payment_transfer_service.dto.TransferResult;
import com.example.payment_transfer_service.entity.Transaction;
import com.example.payment_transfer_service.service.PaymentTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentTransferService paymentTransferService;

    @PostMapping("/transfer")
    public ResponseEntity<TransferResult> transferFunds(@Valid @RequestBody TransferRequest request) {
        TransferResult result = paymentTransferService.transferFunds(request);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable String transactionId) {
        try {
            Transaction transaction = paymentTransferService.getTransactionById(transactionId);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
