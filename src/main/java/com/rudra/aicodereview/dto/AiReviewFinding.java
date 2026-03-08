package com.rudra.aicodereview.dto;

import com.rudra.aicodereview.entity.Severity;

public record AiReviewFinding(
    Severity severity,
    String ruleId,
    String message,
    Integer lineStart,
    Integer lineEnd,
    String suggestion
) {}

