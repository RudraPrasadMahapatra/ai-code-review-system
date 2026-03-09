package com.rudra.aicodereview.dto;

import com.rudra.aicodereview.entity.IssueCategory;
import com.rudra.aicodereview.entity.Severity;

public record AiReviewFinding(
    Severity severity,
    IssueCategory category,
    String ruleId,
    String message,
    Integer lineStart,
    Integer lineEnd,
    String suggestion
) {}
