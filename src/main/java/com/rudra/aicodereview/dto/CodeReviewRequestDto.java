package com.rudra.aicodereview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CodeReviewRequestDto(
    @NotBlank @Size(max = 50) String language,
    @NotBlank @Size(max = 200_000) String code
) {}

