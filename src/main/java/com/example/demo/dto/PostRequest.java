package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostRequest(
    @NotNull Long authorId,
    @NotBlank String text
) {}