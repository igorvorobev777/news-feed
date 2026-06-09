package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportRequest(
    @NotNull(message = "postId is required")
    Long postId,
    
    @NotNull(message = "userId is required")
    Long userId,
    
    @NotBlank(message = "Reason is required")
    @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
    String reason
) {}