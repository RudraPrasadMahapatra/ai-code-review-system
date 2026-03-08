package com.rudra.aicodereview.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "code_reviews")
public class CodeReviewRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50)
  private String language;

  @Column(length = 500)
  private String filePath;

  @Lob
  @Column(nullable = false)
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private CodeReviewStatus status;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @OneToMany(mappedBy = "codeReview", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CodeIssue> findings = new ArrayList<>();

  @PrePersist
  void prePersist() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
    if (status == null) {
      status = CodeReviewStatus.PENDING;
    }
  }

  public void addFinding(CodeIssue finding) {
    finding.setCodeReview(this);
    findings.add(finding);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public CodeReviewStatus getStatus() {
    return status;
  }

  public void setStatus(CodeReviewStatus status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public List<CodeIssue> getFindings() {
    return findings;
  }

  public void setFindings(List<CodeIssue> findings) {
    this.findings = findings;
  }
}

