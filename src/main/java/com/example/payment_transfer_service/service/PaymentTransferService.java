package com.example.payment_transfer_service.service;

import com.example.payment_transfer_service.dto.TransferRequest;
import com.example.payment_transfer_service.dto.UserTransferRequest;
import com.example.payment_transfer_service.dto.TransferResult;
import com.example.payment_transfer_service.dto.UserTransactionHistory;
import com.example.payment_transfer_service.entity.*;
import com.example.payment_transfer_service.exception.*;
import com.example.payment_transfer_service.repository.AccountRepository;
import com.example.payment_transfer_service.repository.TransactionRepository;
import com.example.payment_transfer_service.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentTransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AccountService accountService;

    @Transactional
    public TransferResult transferFunds(UserTransferRequest request) {
        log.info("Starting user transfer from {} to {} for amount {} by user {}",
                request.getSourceAccountId(), request.getDestinationAccountId(),
                request.getAmount(), request.getUserId());

        try {
            validateUserTransferRequest(request);

            TransactionType transactionType = determineTransactionType(request);

            Transaction transaction = createPendingUserTransaction(request, transactionType);

            processUserTransfer(request, transaction);

            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);

            log.info("User transfer completed successfully. Transaction ID: {}", transaction.getId());

            return TransferResult.success(transaction.getId(),
                    "Transfer completed successfully", transactionType);

        } catch (PaymentException e) {
            log.error("User transfer failed: {}", e.getMessage());
            return TransferResult.failure(e.getMessage(), e.getErrorCode());

        } catch (Exception e) {
            log.error("Unexpected error during user transfer", e);
            return TransferResult.failure("Internal server error", "INTERNAL_ERROR");
        }
    }

    @Transactional
    public TransferResult transferFunds(TransferRequest request) {
        log.info("Starting legacy transfer from {} to {} for amount {}",
                request.getSourceAccountId(), request.getDestinationAccountId(), request.getAmount());

        try {
            validateTransferRequest(request);
            Transaction transaction = createPendingTransaction(request);

            String firstAccountId = request.getSourceAccountId().compareTo(request.getDestinationAccountId()) < 0
                    ? request.getSourceAccountId() : request.getDestinationAccountId();
            String secondAccountId = request.getSourceAccountId().equals(firstAccountId)
                    ? request.getDestinationAccountId() : request.getSourceAccountId();

            Account firstAccount = lockAndGetAccount(firstAccountId);
            Account secondAccount = lockAndGetAccount(secondAccountId);

            Account sourceAccount = request.getSourceAccountId().equals(firstAccountId)
                    ? firstAccount : secondAccount;
            Account destinationAccount = request.getDestinationAccountId().equals(firstAccountId)
                    ? firstAccount : secondAccount;

            validateAccountsForTransfer(sourceAccount, destinationAccount, request.getAmount());
            processTransfer(sourceAccount, destinationAccount, request.getAmount());

            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);

            log.info("Legacy transfer completed successfully. Transaction ID: {}", transaction.getId());

            return TransferResult.success(transaction.getId(), "Transfer completed successfully");

        } catch (PaymentException e) {
            log.error("Legacy transfer failed: {}", e.getMessage());
            return TransferResult.failure(e.getMessage(), e.getErrorCode());

        } catch (Exception e) {
            log.error("Unexpected error during legacy transfer", e);
            return TransferResult.failure("Internal server error", "INTERNAL_ERROR");
        }
    }

    @Transactional(readOnly = true)
    public List<UserTransactionHistory> getUserTransactionHistory(String userId) {
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return transactions.stream()
                .map(transaction -> mapToUserTransactionHistory(transaction, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<UserTransactionHistory> getUserTransactionHistory(String userId, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return transactions.map(transaction -> mapToUserTransactionHistory(transaction, userId));
    }

    @Transactional(readOnly = true)
    public List<UserTransactionHistory> getAccountTransactionHistory(String accountId, String userId) {
        // Validate account ownership
        if (!accountService.validateAccountOwnership(accountId, userId)) {
            throw new PaymentException("Account access denied", "ACCOUNT_ACCESS_DENIED");
        }

        List<Transaction> transactions = transactionRepository.findAccountTransactionsByUserId(accountId, userId);
        return transactions.stream()
                .map(transaction -> mapToUserTransactionHistory(transaction, userId, accountId))
                .collect(Collectors.toList());
    }

    private void validateUserTransferRequest(UserTransferRequest request) {
        // Validate user exists and is active
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new PaymentException("User not found", "USER_NOT_FOUND"));

        if (!user.isActive()) {
            throw new PaymentException("User account is not active", "USER_INACTIVE");
        }

        // Validate accounts belong to user
        if (!accountService.validateAccountOwnership(request.getSourceAccountId(), request.getUserId())) {
            throw new PaymentException("Source account access denied", "ACCOUNT_ACCESS_DENIED");
        }

        if (!accountService.validateAccountOwnership(request.getDestinationAccountId(), request.getUserId())) {
            throw new PaymentException("Destination account access denied", "ACCOUNT_ACCESS_DENIED");
        }

        // Basic validations
        if (request.getSourceAccountId().equals(request.getDestinationAccountId())) {
            throw new PaymentException("Source and destination accounts cannot be the same", "SAME_ACCOUNT");
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("Transfer amount must be positive", "INVALID_AMOUNT");
        }

        // Check for duplicate reference
        if (request.getReference() != null &&
                transactionRepository.existsByReferenceAndUserId(request.getReference(), request.getUserId())) {
            throw new PaymentException("Reference number already exists for this user", "DUPLICATE_REFERENCE");
        }
    }

    private TransactionType determineTransactionType(UserTransferRequest request) {
        return TransactionType.INTERNAL_TRANSFER;
    }

    private Transaction createPendingUserTransaction(UserTransferRequest request, TransactionType type) {
        Transaction transaction = new Transaction();
        transaction.setUserId(request.getUserId());
        transaction.setSourceAccountId(request.getSourceAccountId());
        transaction.setDestinationAccountId(request.getDestinationAccountId());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency("USD");
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setTransactionType(type);
        transaction.setDescription(request.getDescription());
        transaction.setReference(request.getReference());

        return transactionRepository.save(transaction);
    }

    private void processUserTransfer(UserTransferRequest request, Transaction transaction) {
        String firstAccountId = request.getSourceAccountId().compareTo(request.getDestinationAccountId()) < 0
                ? request.getSourceAccountId() : request.getDestinationAccountId();
        String secondAccountId = request.getSourceAccountId().equals(firstAccountId)
                ? request.getDestinationAccountId() : request.getSourceAccountId();

        Account firstAccount = lockAndGetAccount(firstAccountId);
        Account secondAccount = lockAndGetAccount(secondAccountId);

        Account sourceAccount = request.getSourceAccountId().equals(firstAccountId)
                ? firstAccount : secondAccount;
        Account destinationAccount = request.getDestinationAccountId().equals(firstAccountId)
                ? firstAccount : secondAccount;

        validateUserAccountsForTransfer(sourceAccount, destinationAccount, request.getAmount(), request.getUserId());

        processTransfer(sourceAccount, destinationAccount, request.getAmount());
    }

    private void validateUserAccountsForTransfer(Account sourceAccount, Account destinationAccount,
                                                 BigDecimal amount, String userId) {
        if (!sourceAccount.belongsToUser(userId)) {
            throw new PaymentException("Source account access denied", "ACCOUNT_ACCESS_DENIED");
        }

        if (!destinationAccount.belongsToUser(userId)) {
            throw new PaymentException("Destination account access denied", "ACCOUNT_ACCESS_DENIED");
        }

        validateAccountsForTransfer(sourceAccount, destinationAccount, amount);
    }

    private UserTransactionHistory mapToUserTransactionHistory(Transaction transaction, String userId) {
        return mapToUserTransactionHistory(transaction, userId, null);
    }

    private UserTransactionHistory mapToUserTransactionHistory(Transaction transaction, String userId, String focusAccountId) {
        return UserTransactionHistory.builder()
                .id(transaction.getId())
                .sourceAccountId(transaction.getSourceAccountId())
                .sourceAccountName(getAccountDisplayName(transaction.getSourceAccountId()))
                .destinationAccountId(transaction.getDestinationAccountId())
                .destinationAccountName(getAccountDisplayName(transaction.getDestinationAccountId()))
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .status(transaction.getStatus())
                .transactionType(transaction.getTransactionType())
                .description(transaction.getDescription())
                .reference(transaction.getReference())
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .build();
    }

    private String getAccountDisplayName(String accountId) {
        return accountRepository.findById(accountId)
                .map(Account::getDisplayName)
                .orElse("Unknown Account");
    }

    private void validateTransferRequest(TransferRequest request) {
        if (request.getSourceAccountId().equals(request.getDestinationAccountId())) {
            throw new PaymentException("Source and destination accounts cannot be the same", "SAME_ACCOUNT");
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("Transfer amount must be positive", "INVALID_AMOUNT");
        }
    }

    private Transaction createPendingTransaction(TransferRequest request) {
        Transaction transaction = new Transaction();
        transaction.setUserId("SYSTEM");
        transaction.setSourceAccountId(request.getSourceAccountId());
        transaction.setDestinationAccountId(request.getDestinationAccountId());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setTransactionType(TransactionType.EXTERNAL_TRANSFER);
        transaction.setDescription(request.getDescription());
        transaction.setReference(request.getReference());

        return transactionRepository.save(transaction);
    }

    private Account lockAndGetAccount(String accountId) {
        return accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private void validateAccountsForTransfer(Account sourceAccount, Account destinationAccount, BigDecimal amount) {
        if (sourceAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountInactiveException(sourceAccount.getId(), sourceAccount.getStatus().toString());
        }

        if (destinationAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountInactiveException(destinationAccount.getId(), destinationAccount.getStatus().toString());
        }

        if (!sourceAccount.getCurrency().equals(destinationAccount.getCurrency())) {
            throw new CurrencyMismatchException(sourceAccount.getCurrency(), destinationAccount.getCurrency());
        }

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(sourceAccount.getId(), sourceAccount.getBalance(), amount);
        }
    }

    private void processTransfer(Account sourceAccount, Account destinationAccount, BigDecimal amount) {
        BigDecimal newSourceBalance = sourceAccount.getBalance().subtract(amount);
        sourceAccount.setBalance(newSourceBalance);
        accountRepository.save(sourceAccount);

        BigDecimal newDestinationBalance = destinationAccount.getBalance().add(amount);
        destinationAccount.setBalance(newDestinationBalance);
        accountRepository.save(destinationAccount);
    }

    public Transaction getTransactionById(String transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new PaymentException("Transaction not found", "TRANSACTION_NOT_FOUND"));
    }
}
