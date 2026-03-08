package com.rudra.aicodereview.service;

import com.rudra.aicodereview.dto.AiReviewFinding;
import com.rudra.aicodereview.dto.CodeReviewRequestDto;
import com.rudra.aicodereview.dto.CodeReviewResponseDto;
import com.rudra.aicodereview.entity.Severity;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AiCodeReviewService {
  private final CodeReviewEngine aiReviewEngine;

  public AiCodeReviewService(CodeReviewEngine aiReviewEngine) {
    this.aiReviewEngine = aiReviewEngine;
  }

  public CodeReviewResponseDto review(CodeReviewRequestDto request) {
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
    return new CodeReviewResponseDto(summary, findings);
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

