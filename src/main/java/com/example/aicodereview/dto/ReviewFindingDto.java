package com.example.aicodereview.dto;

import com.example.aicodereview.entity.Severity;

public record ReviewFindingDto(
    Long id,
    Severity severity,
    String ruleId,
    String message,
    Integer lineStart,
    Integer lineEnd,
    String suggestion
) {}

