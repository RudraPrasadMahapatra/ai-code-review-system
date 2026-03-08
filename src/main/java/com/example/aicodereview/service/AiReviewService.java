package com.example.aicodereview.service;

import com.example.aicodereview.dto.AiReviewFinding;
import com.example.aicodereview.dto.AiReviewRequest;
import com.example.aicodereview.dto.AiReviewResponse;
import com.example.aicodereview.entity.Severity;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AiReviewService {
  private final AiReviewEngine aiReviewEngine;

  public AiReviewService(AiReviewEngine aiReviewEngine) {
    this.aiReviewEngine = aiReviewEngine;
  }

  public AiReviewResponse review(AiReviewRequest request) {
    List<AiReviewFinding> findings =
        aiReviewEngine.review(request.language(), request.code(), null).stream()
            .map(
                finding ->
                    new AiReviewFinding(
                        finding.severity(),
                        finding.ruleId(),
                        finding.message(),
                        finding.lineStart(),
                        finding.lineEnd(),
                        finding.suggestion()))
            .toList();

    String summary = summarize(findings);
    return new AiReviewResponse(summary, findings);
  }

  private static String summarize(List<AiReviewFinding> findings) {
    if (findings.isEmpty()) {
      return "No issues found.";
    }
    long errors = findings.stream().filter(f -> f.severity() == Severity.ERROR).count();
    long warnings = findings.stream().filter(f -> f.severity() == Severity.WARNING).count();
    long infos = findings.stream().filter(f -> f.severity() == Severity.INFO).count();
    return "Findings: %d error(s), %d warning(s), %d info.".formatted(errors, warnings, infos);
  }
}

