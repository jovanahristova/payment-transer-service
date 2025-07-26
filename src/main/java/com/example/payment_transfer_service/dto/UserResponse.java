package com.example.payment_transfer_service.dto;

import com.example.payment_transfer_service.entity.UserStatus;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AccountSummary> accounts;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
