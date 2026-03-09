package com.rudra.aicodereview.service;

import com.rudra.aicodereview.entity.Severity;
import com.rudra.aicodereview.entity.IssueCategory;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "aicodereview.ai", name = "provider", havingValue = "stub", matchIfMissing = true)
public class LocalReviewEngine implements CodeReviewEngine {
  @Override
  public ReviewResponse review(String language, String content, String filePath) {
    long start = System.currentTimeMillis();
    List<Finding> findings = new ArrayList<>();

    if (content == null || content.isBlank()) {
      return emptyResponse();
    }

    int todoIndex = content.indexOf("TODO");
    if (todoIndex >= 0) {
      findings.add(
          new Finding(
              Severity.INFO,
              IssueCategory.OTHER,
              "STUB_TODO",
              "Found TODO marker; consider addressing before merge.",
              null,
              null,
              "Resolve TODOs or create a tracked ticket/issue."));
    }

    if (content.contains("System.out.println")) {
      findings.add(
          new Finding(
              Severity.WARNING,
              IssueCategory.STYLE,
              "STUB_STDOUT",
              "Avoid using System.out.println in production code.",
              null,
              null,
              "Use a logger (e.g., SLF4J) with appropriate log levels."));
    }

    if (content.toLowerCase().contains("password") && content.contains("=")) {
      findings.add(
          new Finding(
              Severity.ERROR,
              IssueCategory.SECURITY,
              "STUB_SECRET",
              "Potential hardcoded secret detected.",
              null,
              null,
              "Move secrets to environment variables or a secrets manager; avoid committing them."));
    }

    long executionTimeMs = System.currentTimeMillis() - start;
    return new ReviewResponse(
        8,
        "O(N)",
        "O(1)",
        "// Optimized version of the code (STUB)\n" + content,
        findings,
        150,
        executionTimeMs
    );
  }

  private ReviewResponse emptyResponse() {
      return new ReviewResponse(null, null, null, null, new ArrayList<>(), 0, 0L);
  }
}
