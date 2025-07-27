package com.example.payment_transfer_service.service;

import com.example.payment_transfer_service.dto.AccountCreationRequest;
import com.example.payment_transfer_service.dto.AccountSummary;
import com.example.payment_transfer_service.entity.Account;
import com.example.payment_transfer_service.entity.AccountStatus;
import com.example.payment_transfer_service.entity.User;
import com.example.payment_transfer_service.exception.PaymentException;
import com.example.payment_transfer_service.repository.AccountRepository;
import com.example.payment_transfer_service.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional
    public AccountSummary createAccount(AccountCreationRequest request) {
        log.info("Creating new account for user: {}", request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new PaymentException("User not found", "USER_NOT_FOUND"));

        if (!user.isActive()) {
            throw new PaymentException("User is not active", "USER_INACTIVE");
        }

        Account account = new Account();
        account.setId(generateAccountId());
        account.setUser(user);
        account.setAccountName(request.getAccountName());
        account.setAccountType(request.getAccountType());
        account.setBalance(request.getInitialBalance());
        account.setCurrency(request.getCurrency());
        account.setStatus(AccountStatus.ACTIVE);
        account.setVersion(0L);

        Account savedAccount = accountRepository.save(account);

        log.info("Created account successfully with ID: {}", savedAccount.getId());

        return mapToAccountSummary(savedAccount);
    }

    @Transactional(readOnly = true)
    public List<AccountSummary> getUserAccounts(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new PaymentException("User not found", "USER_NOT_FOUND");
        }

        return accountRepository.findActiveAccountsByUserId(userId).stream()
                .map(this::mapToAccountSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountSummary getAccountById(String accountId, String userId) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new PaymentException("Account not found or access denied", "ACCOUNT_ACCESS_DENIED"));

        return mapToAccountSummary(account);
    }

    @Transactional
    public AccountSummary updateAccountStatus(String accountId, String userId, AccountStatus status) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new PaymentException("Account not found or access denied", "ACCOUNT_ACCESS_DENIED"));

        account.setStatus(status);
        Account updatedAccount = accountRepository.save(account);

        log.info("Updated account {} status to {}", accountId, status);

        return mapToAccountSummary(updatedAccount);
    }

    @Transactional(readOnly = true)
    public boolean validateAccountOwnership(String accountId, String userId) {
        return accountRepository.findByIdAndUserId(accountId, userId).isPresent();
    }


    private String generateAccountId() {
        return "ACC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private AccountSummary mapToAccountSummary(Account account) {
        return AccountSummary.builder()
                .id(account.getId())
                .accountName(account.getAccountName())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
