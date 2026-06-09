package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;

public record LikeRequest(
    @NotNull(message = "userId is required")
    Long userId
) {}