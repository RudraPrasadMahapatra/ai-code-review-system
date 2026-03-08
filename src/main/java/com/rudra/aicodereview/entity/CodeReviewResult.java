package com.rudra.aicodereview.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(
    name = "review_results",
    indexes = {@Index(name = "idx_review_results_submission_id", columnList = "code_submission_id")})
public class CodeReviewResult {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "code_submission_id", nullable = false, unique = true)
  private SourceCodeSubmission codeSubmission;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ReviewStatus status;

  @Column(length = 100)
  private String model;

  @Lob
  private String summary;

  @Lob
  private String rawOutput;

  private Instant completedAt;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    if (createdAt == null) {
      createdAt = now;
    }
    if (updatedAt == null) {
      updatedAt = now;
    }
    if (status == null) {
      status = ReviewStatus.PENDING;
    }
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public SourceCodeSubmission getCodeSubmission() {
    return codeSubmission;
  }

  public void setCodeSubmission(SourceCodeSubmission codeSubmission) {
    this.codeSubmission = codeSubmission;
  }

  public ReviewStatus getStatus() {
    return status;
  }

  public void setStatus(ReviewStatus status) {
    this.status = status;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getRawOutput() {
    return rawOutput;
  }

  public void setRawOutput(String rawOutput) {
    this.rawOutput = rawOutput;
  }

  public Instant getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(Instant completedAt) {
    this.completedAt = completedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}

