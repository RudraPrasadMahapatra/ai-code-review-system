package com.example.aicodereview.service;

import com.example.aicodereview.entity.Severity;
import java.util.List;

public interface AiReviewEngine {
  record Finding(
      Severity severity,
      String ruleId,
      String message,
      Integer lineStart,
      Integer lineEnd,
      String suggestion
  ) {}

  List<Finding> review(String language, String content, String filePath);
}

