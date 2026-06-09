package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Login is required")
        @Size(min = 3, max = 50, message = "Login must be between 3 and 50 characters")
        String login,
        
        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
        String password,
        
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email
) {}