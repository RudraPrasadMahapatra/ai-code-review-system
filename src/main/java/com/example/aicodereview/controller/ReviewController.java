package com.example.aicodereview.controller;

import com.example.aicodereview.dto.AiReviewRequest;
import com.example.aicodereview.dto.AiReviewResponse;
import com.example.aicodereview.service.AiReviewService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewController {
  private final AiReviewService aiReviewService;

  public ReviewController(AiReviewService aiReviewService) {
    this.aiReviewService = aiReviewService;
  }

  @PostMapping(value = "/review", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AiReviewResponse> review(@Valid @RequestBody AiReviewRequest request) {
    AiReviewResponse response = aiReviewService.review(request);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}
