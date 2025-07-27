package com.example.payment_transfer_service.controller;

import com.example.payment_transfer_service.dto.*;
import com.example.payment_transfer_service.entity.AccountStatus;
import com.example.payment_transfer_service.service.AccountService;
import com.example.payment_transfer_service.service.PaymentTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Management", description = "Banking account operations and management")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;
    private final PaymentTransferService paymentTransferService;

    @Operation(
            summary = "Create new account",
            description = "Create a new banking account for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Account created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountSummary.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid account creation request",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<AccountSummary> createAccount(
            @Parameter(description = "Account creation details", required = true)
            @Valid @RequestBody AccountCreationRequest request) {
        AccountSummary account = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @Operation(
            summary = "Get user accounts",
            description = "Retrieve all accounts belonging to a specific user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Accounts retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AccountSummary.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountSummary>> getUserAccounts(
            @Parameter(description = "User ID to retrieve accounts for", required = true, example = "user123")
            @PathVariable String userId) {
        List<AccountSummary> accounts = accountService.getUserAccounts(userId);
        return ResponseEntity.ok(accounts);
    }

    @Operation(
            summary = "Get account details",
            description = "Retrieve detailed information about a specific account"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Account details retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountSummary.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Account does not belong to user",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found",
                    content = @Content
            )
    })
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountSummary> getAccount(
            @Parameter(description = "Account ID to retrieve", required = true, example = "acc123")
            @PathVariable String accountId,
            @Parameter(description = "User ID for authorization", required = true, example = "user123")
            @RequestParam String userId) {
        AccountSummary account = accountService.getAccountById(accountId, userId);
        return ResponseEntity.ok(account);
    }

    @Operation(
            summary = "Update account status",
            description = "Change the status of an account (ACTIVE, SUSPENDED, CLOSED)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Account status updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountSummary.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status or request",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Account does not belong to user",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found",
                    content = @Content
            )
    })
    @PutMapping("/{accountId}/status")
    public ResponseEntity<AccountSummary> updateAccountStatus(
            @Parameter(description = "Account ID to update", required = true, example = "acc123")
            @PathVariable String accountId,
            @Parameter(description = "User ID for authorization", required = true, example = "user123")
            @RequestParam String userId,
            @Parameter(description = "New account status", required = true)
            @RequestParam AccountStatus status) {
        AccountSummary account = accountService.updateAccountStatus(accountId, userId, status);
        return ResponseEntity.ok(account);
    }

    @Operation(
            summary = "Get account transactions",
            description = "Retrieve transaction history for a specific account"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Account transactions retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserTransactionHistory.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Account does not belong to user",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found",
                    content = @Content
            )
    })
    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<List<UserTransactionHistory>> getAccountTransactions(
            @Parameter(description = "Account ID to get transactions for", required = true, example = "acc123")
            @PathVariable String accountId,
            @Parameter(description = "User ID for authorization", required = true, example = "user123")
            @RequestParam String userId) {
        List<UserTransactionHistory> transactions =
                paymentTransferService.getAccountTransactionHistory(accountId, userId);
        return ResponseEntity.ok(transactions);
    }
}
