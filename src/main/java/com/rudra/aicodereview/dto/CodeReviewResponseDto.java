package com.rudra.aicodereview.dto;

import java.util.List;

public record CodeReviewResponseDto(
    String summary,
    Integer score,
    String timeComplexity,
    String spaceComplexity,
    String optimizedCode,
    List<AiReviewFinding> findings
) {}

