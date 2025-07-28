package com.example.payment_transfer_service.controller;

import com.example.payment_transfer_service.dto.*;
import com.example.payment_transfer_service.service.UserService;
import com.example.payment_transfer_service.service.PaymentTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PaymentTransferService paymentTransferService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{userId}/transactions")
    public ResponseEntity<List<UserTransactionHistory>> getUserTransactions(@PathVariable String userId) {
        List<UserTransactionHistory> transactions = paymentTransferService.getUserTransactionHistory(userId);
        return ResponseEntity.ok(transactions);
    }

}
