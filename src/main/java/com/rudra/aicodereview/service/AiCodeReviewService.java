package com.rudra.aicodereview.service;

import com.rudra.aicodereview.dto.AiReviewFinding;
import com.rudra.aicodereview.dto.CodeReviewRequestDto;
import com.rudra.aicodereview.dto.CodeReviewResponseDto;
import com.rudra.aicodereview.entity.Severity;
import com.rudra.aicodereview.entity.CodeIssue;
import com.rudra.aicodereview.entity.CodeReviewRecord;
import com.rudra.aicodereview.entity.CodeReviewStatus;
import com.rudra.aicodereview.repository.CodeReviewRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiCodeReviewService {
  private final CodeReviewEngine aiReviewEngine;
  private final CodeReviewRepository repository;

  public AiCodeReviewService(CodeReviewEngine aiReviewEngine, CodeReviewRepository repository) {
    this.aiReviewEngine = aiReviewEngine;
    this.repository = repository;
  }

  @Transactional
  public CodeReviewResponseDto review(CodeReviewRequestDto request) {
    CodeReviewRecord record = new CodeReviewRecord();
    record.setLanguage(request.language());
    record.setContent(request.code());
    record.setFilePath("UI_SUBMISSION");
    record.setStatus(CodeReviewStatus.PENDING);

    CodeReviewEngine.ReviewResponse engineResponse =
        aiReviewEngine.review(request.language(), request.code(), null);

    record.setScore(engineResponse.score());
    record.setTimeComplexity(engineResponse.timeComplexity());
    record.setSpaceComplexity(engineResponse.spaceComplexity());
    record.setTokensUsed(engineResponse.tokensUsed());
    record.setExecutionTimeMs(engineResponse.executionTimeMs());

    List<AiReviewFinding> findings =
        engineResponse.findings().stream()
            .map(
                finding -> {
                    AiReviewFinding mappedFinding = new AiReviewFinding(
                        finding.severity(),
                        finding.category(),
                        finding.ruleId(),
                        finding.message(),
                        finding.lineStart(),
                        finding.lineEnd(),
                        finding.suggestion());
        
        CodeIssue issue = new CodeIssue();
        issue.setSeverity(finding.severity());
        issue.setCategory(finding.category());
        issue.setRuleId(finding.ruleId());
        issue.setMessage(finding.message());
        issue.setLineStart(finding.lineStart());
        issue.setLineEnd(finding.lineEnd());
        issue.setSuggestion(finding.suggestion());
        record.addFinding(issue);
        
        return mappedFinding;
      })
      .toList();

    record.setStatus(CodeReviewStatus.COMPLETED);
    repository.save(record);

    String summary = summarize(findings);
    return new CodeReviewResponseDto(
        summary,
        engineResponse.score(),
        engineResponse.timeComplexity(),
        engineResponse.spaceComplexity(),
        engineResponse.optimizedCode(),
        findings);
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

