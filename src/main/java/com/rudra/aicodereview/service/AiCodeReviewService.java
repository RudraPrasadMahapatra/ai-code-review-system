package com.rudra.aicodereview.service;

import com.rudra.aicodereview.dto.AiReviewFinding;
import com.rudra.aicodereview.dto.CodeReviewRequestDto;
import com.rudra.aicodereview.dto.CodeReviewResponseDto;
import com.rudra.aicodereview.entity.Severity;
import com.rudra.aicodereview.entity.CodeIssue;
import com.rudra.aicodereview.entity.CodeReviewRecord;
import com.rudra.aicodereview.entity.CodeReviewStatus;
import com.rudra.aicodereview.repository.CodeReviewRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiCodeReviewService {
  private final CodeReviewEngine aiReviewEngine;
  private final CodeReviewRepository repository;
  private final StaticAnalyzer staticAnalyzer;

  public AiCodeReviewService(CodeReviewEngine aiReviewEngine, CodeReviewRepository repository, StaticAnalyzer staticAnalyzer) {
    this.aiReviewEngine = aiReviewEngine;
    this.repository = repository;
    this.staticAnalyzer = staticAnalyzer;
  }

  private String computeHash(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      return String.valueOf(input.hashCode());
    }
  }

  @Transactional
  public CodeReviewResponseDto review(CodeReviewRequestDto request) {
    String fileCode = request.code();
    String contentHash = computeHash(fileCode);

    CodeReviewRecord cachedRecord = repository.findFirstByContentHash(contentHash);
    if (cachedRecord != null && cachedRecord.getStatus() == CodeReviewStatus.COMPLETED) {
      return toDto(cachedRecord);
    }

    CodeReviewRecord record = new CodeReviewRecord();
    record.setLanguage(request.language());
    record.setContent(fileCode);
    record.setContentHash(contentHash);
    record.setFilePath("UI_SUBMISSION");
    record.setStatus(CodeReviewStatus.PENDING);

    // 1. Static Analysis
    List<StaticAnalyzer.StaticIssue> staticIssues = staticAnalyzer.analyze(fileCode);

    // 2. Context Window Reduction
    String codeToAnalyze = fileCode;
    if (codeToAnalyze.length() > 2000) {
      if (!staticIssues.isEmpty()) {
        int minStart = codeToAnalyze.length();
        int maxEnd = 0;
        for (StaticAnalyzer.StaticIssue issue : staticIssues) {
          minStart = Math.min(minStart, issue.startIndex());
          maxEnd = Math.max(maxEnd, issue.endIndex());
        }
        int start = Math.max(0, minStart - 500);
        int end = Math.min(codeToAnalyze.length(), maxEnd + 500);
        if (end - start > 2000) {
          end = start + 2000;
          if (end > codeToAnalyze.length()) {
            end = codeToAnalyze.length();
            start = Math.max(0, end - 2000);
          }
        }
        codeToAnalyze = codeToAnalyze.substring(start, end);
      } else {
        codeToAnalyze = codeToAnalyze.substring(0, 2000);
      }
    }

    CodeReviewEngine.ReviewResponse engineResponse =
        aiReviewEngine.review(request.language(), codeToAnalyze, null);

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
        findings,
        engineResponse.tokensUsed(),
        engineResponse.executionTimeMs());
  }

  private CodeReviewResponseDto toDto(CodeReviewRecord record) {
    List<AiReviewFinding> findings = record.getFindings().stream()
      .map(f -> new AiReviewFinding(f.getSeverity(), f.getCategory(), f.getRuleId(), f.getMessage(), f.getLineStart(), f.getLineEnd(), f.getSuggestion()))
      .toList();
    
    return new CodeReviewResponseDto(
      summarize(findings),
      record.getScore(),
      record.getTimeComplexity(),
      record.getSpaceComplexity(),
      null, // optimized code can be skipped for now on cache unless we save it in db
      findings,
      record.getTokensUsed(),
      record.getExecutionTimeMs()
    );
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

