package com.example.aicodereview.service;

import com.example.aicodereview.dto.CodeReviewRequest;
import com.example.aicodereview.dto.CodeReviewResponse;
import com.example.aicodereview.dto.ReviewFindingDto;
import com.example.aicodereview.entity.CodeReview;
import com.example.aicodereview.entity.CodeReviewStatus;
import com.example.aicodereview.entity.ReviewFinding;
import com.example.aicodereview.exception.ResourceNotFoundException;
import com.example.aicodereview.repository.CodeReviewRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeReviewService {
  private final CodeReviewRepository codeReviewRepository;
  private final AiReviewEngine aiReviewEngine;

  public CodeReviewService(CodeReviewRepository codeReviewRepository, AiReviewEngine aiReviewEngine) {
    this.codeReviewRepository = codeReviewRepository;
    this.aiReviewEngine = aiReviewEngine;
  }

  @Transactional
  public CodeReviewResponse createReview(CodeReviewRequest request) {
    CodeReview review = new CodeReview();
    review.setLanguage(request.language());
    review.setFilePath(request.filePath());
    review.setContent(request.content());
    review.setStatus(CodeReviewStatus.PENDING);
    review = codeReviewRepository.save(review);

    try {
      List<AiReviewEngine.Finding> findings =
          aiReviewEngine.review(request.language(), request.content(), request.filePath());
      for (AiReviewEngine.Finding finding : findings) {
        ReviewFinding entity = new ReviewFinding();
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

    CodeReview saved = codeReviewRepository.save(review);
    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public CodeReviewResponse getReview(long id) {
    CodeReview review =
        codeReviewRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Review " + id + " not found"));
    return toResponse(review);
  }

  private static CodeReviewResponse toResponse(CodeReview review) {
    List<ReviewFindingDto> findingDtos =
        review.getFindings().stream()
            .map(
                finding ->
                    new ReviewFindingDto(
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

