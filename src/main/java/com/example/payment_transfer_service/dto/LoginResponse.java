package com.example.payment_transfer_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private String userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
}
