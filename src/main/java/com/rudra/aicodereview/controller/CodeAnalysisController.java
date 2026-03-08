package com.rudra.aicodereview.controller;

import com.rudra.aicodereview.dto.CodeReviewRequest;
import com.rudra.aicodereview.dto.CodeReviewResponse;
import com.rudra.aicodereview.service.CodeReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
public class CodeAnalysisController {
  private final CodeReviewService codeReviewService;

  public CodeAnalysisController(CodeReviewService codeReviewService) {
    this.codeReviewService = codeReviewService;
  }

  @PostMapping
  public ResponseEntity<CodeReviewResponse> createReview(@Valid @RequestBody CodeReviewRequest request) {
    CodeReviewResponse response = codeReviewService.createReview(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  public CodeReviewResponse getReview(@PathVariable long id) {
    return codeReviewService.getReview(id);
  }
}

