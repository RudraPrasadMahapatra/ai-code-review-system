package com.example.aicodereview.dto;

import com.example.aicodereview.entity.CodeReviewStatus;
import java.time.Instant;
import java.util.List;

public record CodeReviewResponse(
    Long id,
    CodeReviewStatus status,
    Instant createdAt,
    String language,
    String filePath,
    List<ReviewFindingDto> findings
) {}

