package com.example.payment_transfer_service.service;

import com.example.payment_transfer_service.dto.*;
import com.example.payment_transfer_service.entity.User;
import com.example.payment_transfer_service.entity.UserStatus;
import com.example.payment_transfer_service.entity.Account;
import com.example.payment_transfer_service.exception.PaymentException;
import com.example.payment_transfer_service.repository.UserRepository;
import com.example.payment_transfer_service.repository.AccountRepository;

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
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public UserResponse createUser(UserRegistrationRequest request) {
        log.info("Creating new user with username: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new PaymentException("Username already exists", "USERNAME_EXISTS");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new PaymentException("Email already exists", "EMAIL_EXISTS");
        }

        User user = new User();
        user.setId(generateUserId());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setStatus(UserStatus.ACTIVE);
        user.setVersion(0L);

        User savedUser = userRepository.save(user);

        log.info("Created user successfully with ID: {}", savedUser.getId());

        return mapToUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PaymentException("User not found", "USER_NOT_FOUND"));

        return mapToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new PaymentException("User not found", "USER_NOT_FOUND"));

        return mapToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllActiveUsers() {
        return userRepository.findAllActiveUsers().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUserStatus(String userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PaymentException("User not found", "USER_NOT_FOUND"));

        user.setStatus(status);
        User updatedUser = userRepository.save(user);

        log.info("Updated user {} status to {}", userId, status);

        return mapToUserResponse(updatedUser);
    }

    @Transactional(readOnly = true)
    public boolean validateUserExists(String userId) {
        return userRepository.existsById(userId);
    }

    @Transactional(readOnly = true)
    public boolean validateUserActive(String userId) {
        return userRepository.findById(userId)
                .map(user -> user.getStatus() == UserStatus.ACTIVE)
                .orElse(false);
    }

    private String generateUserId() {
        return "USR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private UserResponse mapToUserResponse(User user) {
        List<AccountSummary> accountSummaries = accountRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToAccountSummary)
                .collect(Collectors.toList());

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .accounts(accountSummaries)
                .build();
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
