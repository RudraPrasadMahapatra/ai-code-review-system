package com.rudra.aicodereview.dto;

import com.rudra.aicodereview.entity.CodeReviewStatus;
import java.time.Instant;
import java.util.List;

public record CodeReviewResponse(
    Long id,
    CodeReviewStatus status,
    Instant createdAt,
    String language,
    String filePath,
    List<CodeIssueDto> findings
) {}

