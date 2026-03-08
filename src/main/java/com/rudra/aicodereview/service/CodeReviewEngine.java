package com.rudra.aicodereview.service;

import com.rudra.aicodereview.entity.Severity;
import java.util.List;

public interface CodeReviewEngine {
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

