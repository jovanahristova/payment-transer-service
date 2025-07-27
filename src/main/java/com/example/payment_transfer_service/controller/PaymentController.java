package com.example.payment_transfer_service.controller;

import com.example.payment_transfer_service.dto.TransferRequest;
import com.example.payment_transfer_service.dto.TransferResult;
import com.example.payment_transfer_service.entity.Transaction;
import com.example.payment_transfer_service.service.PaymentTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Operations", description = "Money transfer and payment processing endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentTransferService paymentTransferService;

    @Operation(
            summary = "Transfer funds",
            description = "Transfer money between accounts with real-time processing and validation"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transfer completed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransferResult.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Transfer failed - insufficient funds, invalid accounts, or validation errors",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransferResult.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Not authorized to access specified accounts",
                    content = @Content
            )
    })
    @PostMapping("/transfer")
    public ResponseEntity<TransferResult> transferFunds(
            @Parameter(description = "Transfer request details including amount, source and destination accounts", required = true)
            @Valid @RequestBody TransferRequest request) {
        TransferResult result = paymentTransferService.transferFunds(request);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @Operation(
            summary = "Get transaction details",
            description = "Retrieve detailed information about a specific transaction"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transaction details retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Transaction.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found",
                    content = @Content
            )
    })
    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<Transaction> getTransaction(
            @Parameter(description = "Transaction ID to retrieve", required = true, example = "txn123")
            @PathVariable String transactionId) {
        try {
            Transaction transaction = paymentTransferService.getTransactionById(transactionId);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
