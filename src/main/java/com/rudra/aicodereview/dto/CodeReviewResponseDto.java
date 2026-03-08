package com.rudra.aicodereview.dto;

import java.util.List;

public record CodeReviewResponseDto(
    String summary,
    List<AiReviewFinding> findings
) {}

