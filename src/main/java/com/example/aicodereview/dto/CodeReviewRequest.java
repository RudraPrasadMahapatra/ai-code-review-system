package com.example.aicodereview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CodeReviewRequest(
    @NotBlank @Size(max = 50) String language,
    @Size(max = 500) String filePath,
    @NotBlank @Size(max = 200_000) String content
) {}

