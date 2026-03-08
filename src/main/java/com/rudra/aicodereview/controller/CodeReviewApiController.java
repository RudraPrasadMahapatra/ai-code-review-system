package com.rudra.aicodereview.controller;

import com.rudra.aicodereview.dto.CodeReviewRequestDto;
import com.rudra.aicodereview.dto.CodeReviewResponseDto;
import com.rudra.aicodereview.service.AiCodeReviewService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CodeReviewApiController {
  private final AiCodeReviewService aiReviewService;

  public CodeReviewApiController(AiCodeReviewService aiReviewService) {
    this.aiReviewService = aiReviewService;
  }

  @PostMapping(value = "/review", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<CodeReviewResponseDto> review(@Valid @RequestBody CodeReviewRequestDto request) {
    CodeReviewResponseDto response = aiReviewService.review(request);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}
