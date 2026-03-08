package com.example.aicodereview.dto;

import java.util.List;

public record AiReviewResponse(
    String summary,
    List<AiReviewFinding> findings
) {}

