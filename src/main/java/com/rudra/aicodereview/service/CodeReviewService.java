package com.rudra.aicodereview.service;

import com.rudra.aicodereview.dto.CodeReviewRequest;
import com.rudra.aicodereview.dto.CodeReviewResponse;
import com.rudra.aicodereview.dto.CodeIssueDto;
import com.rudra.aicodereview.entity.CodeReviewRecord;
import com.rudra.aicodereview.entity.CodeReviewStatus;
import com.rudra.aicodereview.entity.CodeIssue;
import com.rudra.aicodereview.exception.ResourceNotFoundException;
import com.rudra.aicodereview.repository.CodeReviewRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeReviewService {
  private final CodeReviewRepository codeReviewRepository;
  private final CodeReviewEngine aiReviewEngine;

  public CodeReviewService(CodeReviewRepository codeReviewRepository, CodeReviewEngine aiReviewEngine) {
    this.codeReviewRepository = codeReviewRepository;
    this.aiReviewEngine = aiReviewEngine;
  }

  @Transactional
  public CodeReviewResponse createReview(CodeReviewRequest request) {
    CodeReviewRecord review = new CodeReviewRecord();
    review.setLanguage(request.language());
    review.setFilePath(request.filePath());
    review.setContent(request.content());
    review.setStatus(CodeReviewStatus.PENDING);
    review = codeReviewRepository.save(review);

    try {
      List<CodeReviewEngine.Finding> findings =
          aiReviewEngine.review(request.language(), request.content(), request.filePath());
      for (CodeReviewEngine.Finding finding : findings) {
        CodeIssue entity = new CodeIssue();
        entity.setSeverity(finding.severity());
        entity.setRuleId(finding.ruleId());
        entity.setMessage(finding.message());
        entity.setLineStart(finding.lineStart());
        entity.setLineEnd(finding.lineEnd());
        entity.setSuggestion(finding.suggestion());
        review.addFinding(entity);
      }
      review.setStatus(CodeReviewStatus.COMPLETED);
    } catch (RuntimeException ex) {
      review.setStatus(CodeReviewStatus.FAILED);
    }

    CodeReviewRecord saved = codeReviewRepository.save(review);
    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public CodeReviewResponse getReview(long id) {
    CodeReviewRecord review =
        codeReviewRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Review " + id + " not found"));
    return toResponse(review);
  }

  private static CodeReviewResponse toResponse(CodeReviewRecord review) {
    List<CodeIssueDto> findingDtos =
        review.getFindings().stream()
            .map(
                finding ->
                    new CodeIssueDto(
                        finding.getId(),
                        finding.getSeverity(),
                        finding.getRuleId(),
                        finding.getMessage(),
                        finding.getLineStart(),
                        finding.getLineEnd(),
                        finding.getSuggestion()))
            .toList();

    return new CodeReviewResponse(
        review.getId(),
        review.getStatus(),
        review.getCreatedAt(),
        review.getLanguage(),
        review.getFilePath(),
        findingDtos);
  }
}

