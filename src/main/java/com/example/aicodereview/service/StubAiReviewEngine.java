package com.example.aicodereview.service;

import com.example.aicodereview.entity.Severity;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "aicodereview.ai", name = "provider", havingValue = "stub", matchIfMissing = true)
public class StubAiReviewEngine implements AiReviewEngine {
  @Override
  public List<Finding> review(String language, String content, String filePath) {
    List<Finding> findings = new ArrayList<>();

    if (content == null || content.isBlank()) {
      return findings;
    }

    int todoIndex = content.indexOf("TODO");
    if (todoIndex >= 0) {
      findings.add(
          new Finding(
              Severity.INFO,
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
              "STUB_SECRET",
              "Potential hardcoded secret detected.",
              null,
              null,
              "Move secrets to environment variables or a secrets manager; avoid committing them."));
    }

    return findings;
  }
}
