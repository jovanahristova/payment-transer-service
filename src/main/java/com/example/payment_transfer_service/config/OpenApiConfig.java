package com.example.payment_transfer_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Development server"),
                        new Server()
                                .url("https://api.bankingsystem.com")
                                .description("Production server")
                ))
                .info(new Info()
                        .title("Payment Transfer Service API")
                        .version("1.0.0")
                        .description("""
                            # Banking System API Documentation
                            
                            This API provides comprehensive banking operations including:
                            - **User Authentication**: Login, registration, and JWT token management
                            - **Account Management**: Create, view, and manage banking accounts
                            - **Payment Processing**: Real-time money transfers between accounts
                            - **Transaction History**: View detailed transaction records
                            - **Audit Trails**: Complete audit logging for compliance
                            
                            ## Authentication
                            Most endpoints require JWT token authentication. Use the `/api/v1/auth/login` endpoint to obtain a token.
                            
                            ## Getting Started
                            1. Register a new user account using `/api/v1/auth/register`
                            2. Login to get your JWT token using `/api/v1/auth/login`
                            3. Use the "Authorize" button above to add your token
                            4. Start making API calls to manage accounts and transfers
                            """)
                        .contact(new Contact()
                                .name("Banking System Team")
                                .email("api-support@bankingsystem.com")
                                .url("https://bankingsystem.com/support"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token authentication. Format: Bearer {token}")));
    }
}
